
package codelets.motivation;

import CommunicationInterface.SensorI;
import br.unicamp.cst.core.entities.Codelet;
import br.unicamp.cst.core.entities.MemoryObject;
import org.cst.cogscore.modules.motivation.MotivationActionSet;
import org.cst.cogscore.modules.motivation.MotivationData;
import org.cst.cogscore.modules.motivation.MotivationEvaluationReport;
import org.cst.cogscore.modules.motivation.MotivationTestRunner;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * CST codelet for the seated tabletop motivation benchmark.
 *
 * This version uses the complete action repertoire:
 *
 *   AM0-AM16: motor and virtual actions
 *   AA0-AA2 : top-down attentional actions
 *
 * The codelet does not evaluate internal motivational variables. It evaluates
 * only externally observable behavior derived from the selected cognitive
 * actions: gaze/focus actions, object interactions, persistence, substitution,
 * latent learning, and devaluation sensitivity.
 *
 * The CoppeliaSim Lua controller still receives a simplified behavioral
 * response:
 *
 *   motivation_marta_response_action = 1 LOOK
 *   motivation_marta_response_action = 2 INTERACT
 *   motivation_marta_response_action = 3 STOP
 *
 * Raw AM/AA action identity is preserved in the Java trace and CSV files.
 */
public class MotivationEvaluationCodelet extends Codelet {

    private MemoryObject actionRequestMO;
    private MemoryObject focusObjectMO;
    private MemoryObject contextMO;
    private MemoryObject evaluationResultMO;
    private MemoryObject evaluationSummaryMO;
    private MemoryObject actionSetMO;

    private final String experimentId;
    private final boolean autoExperimentFromSimulator;
    private final String architectureName;
    private final SensorI vision;
    private final MotivationTestRunner.Config config;

    private MotivationTestRunner runner;
    private int activeExperimentId;

    private int currentEpoch = -1;
    private long localProcCycle = 0L;

    private String activeTrialKey = null;
    private String lastEvaluatedTrialKey = null;

    private MotivationData.TrialContext activeContext = null;
    private MotivationData.TrialTrace activeTrace = null;

    private int lastSeenResponseSeqFromSimulator = -1;
    private int responseSeqToSimulator = 0;

    private String lastActionSignature = null;

    private final List<MotivationEvaluationReport.TrialResult> currentEpochResults =
            new ArrayList<MotivationEvaluationReport.TrialResult>();

    private boolean debug = true;
    private boolean allExperimentsDoneFlushed = false;

    private boolean stepLoggingEnabled = true;
    private PrintWriter stepLogWriter = null;
    private File stepLogFile = null;

    public MotivationEvaluationCodelet(
            String experimentId,
            String architectureName,
            MotivationTestRunner.Config config,
            SensorI vision
    ) {
        this.experimentId = experimentId == null ? "motivation_experiment" : experimentId;
        this.autoExperimentFromSimulator = isAutoExperimentId(this.experimentId);
        this.architectureName = architectureName == null || architectureName.trim().isEmpty()
                ? "unknown"
                : architectureName;
        this.vision = vision;
        this.config = config == null ? new MotivationTestRunner.Config() : config;

        this.activeExperimentId = parseExperimentId(
                this.experimentId,
                MotivationTestRunner.EXP1_PERSISTENCE
        );

        validateExperimentId(this.activeExperimentId);
        this.runner = new MotivationTestRunner(this.activeExperimentId, this.config);

        setTimeStep(50);
    }

    public MotivationEvaluationCodelet(
            int motivationExperimentId,
            String architectureName,
            MotivationTestRunner.Config config,
            SensorI vision
    ) {
        validateExperimentId(motivationExperimentId);

        this.experimentId = Integer.toString(motivationExperimentId);
        this.autoExperimentFromSimulator = false;
        this.architectureName = architectureName == null || architectureName.trim().isEmpty()
                ? "unknown"
                : architectureName;
        this.vision = vision;
        this.config = config == null ? new MotivationTestRunner.Config() : config;

        this.activeExperimentId = motivationExperimentId;
        this.runner = new MotivationTestRunner(this.activeExperimentId, this.config);

        setTimeStep(50);
    }

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public void setStepLoggingEnabled(boolean stepLoggingEnabled) {
        this.stepLoggingEnabled = stepLoggingEnabled;
        if (!stepLoggingEnabled) {
            closeStepLogWriter();
        }
    }

    public File getStepLogFile() {
        return stepLogFile;
    }

    @Override
    public void accessMemoryObjects() {
        actionRequestMO = firstInput(
                "MOTIVATION_ACTION_REQUEST",
                "MOTIVATIONAL_ACTION"
        );

        focusObjectMO = firstInput(
                "MOTIVATION_FOCUS_OBJECT",
                "PERCEPTUAL_FOCUS_OBJECT",
                "FOCUS_OBJECT"
        );

        contextMO = firstOutput(
                "MOTIVATION_TRIAL_CONTEXT",
                "MOTIVATION_CONTEXT"
        );

        evaluationResultMO = firstOutput(
                "MOTIVATION_EVALUATION_RESULT",
                "MOTIVATION_RESULT"
        );

        evaluationSummaryMO = firstOutput(
                "MOTIVATION_EVALUATION_SUMMARY",
                "MOTIVATION_SUMMARY"
        );

        actionSetMO = firstOutput(
                "MOTIVATION_ACTION_SET",
                "MOTIVATION_AVAILABLE_ACTIONS"
        );
    }

    @Override
    public void calculateActivation() {
        /*
         * This codelet is an evaluator/bridge. It does not compete for activation.
         */
    }

    @Override
    public void proc() {
        localProcCycle++;

        try {
            Integer epoch = resolveEpoch();
            if (epoch == null) {
                epoch = Integer.valueOf(0);
            }

            if (currentEpoch < 0) {
                currentEpoch = epoch.intValue();
            } else if (epoch.intValue() != currentEpoch) {
                flushCurrentEpoch(false);
                currentEpoch = epoch.intValue();
                activeTrialKey = null;
                lastEvaluatedTrialKey = null;
                activeContext = null;
                activeTrace = null;
                lastActionSignature = null;
            }

            int experimentForThisCycle = resolveExperimentIdFromSimulator();
            ensureRunnerExperiment(experimentForThisCycle);

            MotivationData.TrialContext context = readTrialContextFromSimulatorSignals();

            if (context == null) {
                writeJavaStepCsv("NO_MOTIVATION_SIGNALS", null, null);
                flushIfAllExperimentsDone();

                if (debug) {
                    System.out.println("[MotivationEvaluationCodelet] waiting: motivation_* simulator signals not available");
                }
                return;
            }

            if (context.episode == 0) {
                context.episode = currentEpoch;
            }

            if (contextMO != null) {
                contextMO.setI(context);
            }

            if (actionSetMO != null) {
                actionSetMO.setI(MotivationActionSet.availableActionsForPhase(context.developmentalPhase));
            }

            updateActiveTrial(context);
            readSimulatorResponseEcho(context);
            sendArchitectureActionIfAvailable(context);

            if (context.trialComplete) {
                evaluateIfNew(context);
                flushIfAllExperimentsDone();
            }

            writeJavaStepCsv("OK", context, activeTrace);

        } catch (Exception e) {
            System.err.println("[MotivationEvaluationCodelet] proc error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void updateActiveTrial(MotivationData.TrialContext context) {
        String key = buildTrialKey(context);

        if (activeTrace == null || activeTrialKey == null || !activeTrialKey.equals(key)) {
            activeTrialKey = key;
            activeContext = context;
            activeTrace = new MotivationData.TrialTrace(context);
            lastActionSignature = null;
            return;
        }

        activeContext = context;
        activeTrace.updateContext(context);
    }

    private void readSimulatorResponseEcho(MotivationData.TrialContext context) {
        Integer seq = readIntegerSignal("motivation_last_response_seq");

        if (seq == null || seq.intValue() == lastSeenResponseSeqFromSimulator) {
            return;
        }

        lastSeenResponseSeqFromSimulator = seq.intValue();

        Integer objectIndex = readIntegerSignal("motivation_last_response_object");
        Integer actionCode = readIntegerSignal("motivation_last_response_action");
        Integer rawCode = readIntegerSignal("motivation_marta_raw_action_code");

        if (objectIndex == null) {
            objectIndex = Integer.valueOf(0);
        }

        if (actionCode == null) {
            actionCode = Integer.valueOf(0);
        }

        MotivationActionSet.ActionId actionId =
                rawCode == null ? null : MotivationActionSet.ActionId.fromCode(rawCode.intValue());

        MotivationData.AgentAction action =
                new MotivationData.AgentAction(actionId, objectIndex.intValue());

        action = action.withFunctionalType(
                MotivationActionSet.FunctionalType.fromCode(actionCode.intValue())
        );

        MotivationData.AgentAction resolved =
                action.resolveAgainstContext(context, objectIndex.intValue());

        if (activeTrace != null) {
            activeTrace.addAction(context, resolved);
        }
    }

    private void sendArchitectureActionIfAvailable(MotivationData.TrialContext context) {
        if (actionRequestMO == null) {
            return;
        }

        Object raw = actionRequestMO.getI();
        if (raw == null) {
            return;
        }

        MotivationData.AgentAction requested = MotivationData.AgentAction.fromObject(raw);
        if (requested == null || requested.actionId == null) {
            return;
        }

        if (!MotivationActionSet.isAvailableInPhase(requested.actionId, context.developmentalPhase)) {
            if (debug) {
                System.out.println("[MotivationEvaluationCodelet] ignored unavailable action in current phase: "
                        + requested.actionId
                        + " phase="
                        + context.developmentalPhase);
            }
            return;
        }

        int focusObject = resolveFocusObject(context);
        MotivationData.AgentAction resolved = requested.resolveAgainstContext(context, focusObject);

        String signature = buildActionSignature(context, resolved);
        if (signature.equals(lastActionSignature)) {
            /*
             * The memory object usually keeps the same value for several proc cycles.
             * To avoid flooding CoppeliaSim with repeated identical responses, the
             * codelet sends a given action once. If the architecture wants to repeat
             * the same action intentionally, it should update the action sequence
             * field or replace the memory content.
             */
            return;
        }

        lastActionSignature = signature;

        if (activeTrace != null) {
            activeTrace.addAction(context, resolved);
        }

        writeIntegerSignal("motivation_marta_raw_action_code", resolved.actionId.code);
        writeIntegerSignal("motivation_marta_action_category_code", resolved.actionId.category.code);
        writeStringSignal("motivation_marta_raw_action_label", resolved.actionId.id);
        writeStringSignal("motivation_marta_action_category", resolved.actionId.category.name());

        if (resolved.functionalType == MotivationActionSet.FunctionalType.NONE) {
            if (debug) {
                System.out.println("[MotivationEvaluationCodelet] logged non-behavioral action: " + resolved);
            }
            return;
        }

        responseSeqToSimulator++;

        writeIntegerSignal("motivation_marta_response_seq", responseSeqToSimulator);
        writeIntegerSignal("motivation_marta_response_object", resolved.resolvedObjectIndex);
        writeIntegerSignal("motivation_marta_response_action", resolved.functionalType.code);

        if (debug) {
            System.out.println("[MotivationEvaluationCodelet] sent action seq="
                    + responseSeqToSimulator
                    + " raw="
                    + resolved.actionId.id
                    + " functional="
                    + resolved.functionalType
                    + " object="
                    + resolved.resolvedObjectIndex);
        }
    }

    private int resolveFocusObject(MotivationData.TrialContext context) {
        if (focusObjectMO != null) {
            Object raw = focusObjectMO.getI();

            if (raw instanceof Number) {
                int idx = ((Number) raw).intValue();
                if (context.isValidObjectIndex(idx)) {
                    return idx;
                }
            }

            if (raw instanceof String) {
                int idx = context.findObjectIndexByLabelOrRole((String) raw);
                if (context.isValidObjectIndex(idx)) {
                    return idx;
                }
            }

            if (raw instanceof MotivationData.ObjectContext) {
                int idx = ((MotivationData.ObjectContext) raw).index;
                if (context.isValidObjectIndex(idx)) {
                    return idx;
                }
            }
        }

        Integer signalFocus = readIntegerSignal("motivation_focus_object");
        if (signalFocus != null && context.isValidObjectIndex(signalFocus.intValue())) {
            return signalFocus.intValue();
        }

        return 0;
    }

    private void evaluateIfNew(MotivationData.TrialContext context) {
        if (activeTrace == null) {
            return;
        }

        String evalKey = buildEvaluationKey(context);
        if (evalKey.equals(lastEvaluatedTrialKey)) {
            return;
        }

        MotivationEvaluationReport.TrialResult result = runner.evaluate(activeTrace);

        currentEpochResults.add(result);
        lastEvaluatedTrialKey = evalKey;

        if (evaluationResultMO != null) {
            evaluationResultMO.setI(result);
        }

        writeCurrentEpochSnapshot(false);

        if (debug) {
            System.out.println("[MotivationEvaluationCodelet] evaluated experiment="
                    + activeExperimentId
                    + " epoch="
                    + currentEpoch
                    + " trial="
                    + result.trialId
                    + " firstRawAction="
                    + result.firstRawAction
                    + " firstInteraction="
                    + result.firstInteractionObjectLabel);
        }
    }

    private MotivationData.TrialContext readTrialContextFromSimulatorSignals() {
        Integer ready = readIntegerSignal("motivation_ready");
        Integer trial = readIntegerSignal("motivation_trial");
        Integer phaseCode = readIntegerSignal("motivation_phase_code");

        if (ready == null && trial == null && phaseCode == null) {
            return null;
        }

        MotivationData.TrialContext c = new MotivationData.TrialContext();

        c.ready = intSignal("motivation_ready", 0) != 0;
        c.episode = intSignal("motivation_episode", currentEpoch < 0 ? 0 : currentEpoch);
        c.trial = intSignal("motivation_trial", 0);
        c.trialId = stringSignal("motivation_trial_id",
                "MOT_E" + activeExperimentId + "_T" + c.trial);

        c.experimentId = intSignal("motivation_exp_id_active", activeExperimentId);
        c.experimentName = MotivationTestRunner.experimentName(c.experimentId);

        c.phase = MotivationData.Phase.fromCode(intSignal("motivation_phase_code", -1));
        c.phaseLabel = stringSignal("motivation_phase", c.phase.name());
        c.currentCycle = longSignal("motivation_current_cycle", localProcCycle);
        c.phaseStartCycle = longSignal("motivation_phase_start_cycle", -1L);

        c.developmentalPhase = intSignal("motivation_developmental_phase", 3);

        c.condition = stringSignal("motivation_condition", "none");
        c.conditionCode = intSignal("motivation_condition_code", 0);

        c.targetObject = intSignal("motivation_target_object", 0);
        c.targetLabel = stringSignal("motivation_target_label", "none");
        c.targetRole = stringSignal("motivation_target_role", "none");

        c.alternativeObject = intSignal("motivation_alternative_object", 0);
        c.alternativeLabel = stringSignal("motivation_alternative_label", "none");

        c.controlObject = intSignal("motivation_control_object", 0);
        c.controlLabel = stringSignal("motivation_control_label", "none");

        c.goalObject = intSignal("motivation_goal_object", 0);
        c.goalLabel = stringSignal("motivation_goal_label", "none");

        c.devaluedObject = intSignal("motivation_devalued_object", 0);
        c.devaluedLabel = stringSignal("motivation_devalued_label", "none");

        c.targetRemoved = booleanSignal("motivation_target_removed", false);
        c.objectBlocked = booleanSignal("motivation_object_blocked", false);
        c.rewardAvailable = booleanSignal("motivation_reward_available", false);
        c.outcomeDevalued = booleanSignal("motivation_outcome_devalued", false);
        c.explorationAllowed = booleanSignal("motivation_exploration_allowed", false);
        c.trialComplete = booleanSignal("motivation_trial_complete", false);
        c.allDone = booleanSignal("motivation_all_done", false);

        c.lastResponseSeq = intSignal("motivation_last_response_seq", -1);
        c.lastResponseObject = intSignal("motivation_last_response_object", 0);
        c.lastResponseAction = intSignal("motivation_last_response_action", 0);

        readObjectContexts(c);

        return c;
    }

    private void readObjectContexts(MotivationData.TrialContext c) {
        c.objects.clear();

        for (int i = 1; i <= config.maxObjects; i++) {
            String label = readStringSignal("motivation_object" + i + "_label");
            String role = readStringSignal("motivation_object" + i + "_role");
            String name = readStringSignal("motivation_object" + i + "_name");

            if (label == null && role == null && name == null) {
                continue;
            }

            MotivationData.ObjectContext obj = new MotivationData.ObjectContext();
            obj.index = i;
            obj.name = name == null ? "object" + i : name;
            obj.label = label == null ? obj.name : label;
            obj.role = role == null ? "unknown" : role;
            obj.x = doubleSignal("motivation_object" + i + "_x", Double.NaN);
            obj.y = doubleSignal("motivation_object" + i + "_y", Double.NaN);
            obj.z = doubleSignal("motivation_object" + i + "_z", Double.NaN);
            obj.target = booleanSignal("motivation_object" + i + "_is_target", false);
            obj.alternative = booleanSignal("motivation_object" + i + "_is_alternative", false);
            obj.goal = booleanSignal("motivation_object" + i + "_is_goal", false);
            obj.blocked = booleanSignal("motivation_object" + i + "_is_blocked", false);
            obj.devalued = booleanSignal("motivation_object" + i + "_is_devalued", false);
            obj.rewarded = booleanSignal("motivation_object" + i + "_is_rewarded", false);

            c.objects.add(obj);
        }

        c.objectCount = c.objects.size();
    }

    private Integer resolveEpoch() {
        try {
            if (vision != null) {
                return vision.getEpoch();
            }
        } catch (Exception e) {
            System.err.println("[MotivationEvaluationCodelet] vision.getEpoch() error: " + e.getMessage());
        }
        return null;
    }

    private int resolveExperimentIdFromSimulator() {
        if (!autoExperimentFromSimulator) {
            return activeExperimentId;
        }

        Integer active = readIntegerSignal("motivation_exp_id_active");
        if (active != null && isValidExperimentId(active.intValue())) {
            return active.intValue();
        }

        Integer requested = readIntegerSignal("motivation_exp_id");
        if (requested != null && isValidExperimentId(requested.intValue())) {
            return requested.intValue();
        }

        return activeExperimentId;
    }

    private void ensureRunnerExperiment(int experimentId) {
        if (!isValidExperimentId(experimentId)) {
            experimentId = MotivationTestRunner.EXP1_PERSISTENCE;
        }

        if (runner != null && experimentId == activeExperimentId) {
            return;
        }

        flushCurrentEpoch(false);

        activeExperimentId = experimentId;
        runner = new MotivationTestRunner(activeExperimentId, config);

        activeTrialKey = null;
        lastEvaluatedTrialKey = null;
        activeContext = null;
        activeTrace = null;
        lastActionSignature = null;

        if (debug) {
            System.out.println("[MotivationEvaluationCodelet] switched to motivation experiment "
                    + activeExperimentId);
        }
    }

    private String buildTrialKey(MotivationData.TrialContext c) {
        return c.experimentId + "::" + c.episode + "::" + c.trialId;
    }

    private String buildEvaluationKey(MotivationData.TrialContext c) {
        return c.experimentId + "::" + currentEpoch + "::" + c.trialId + "::complete";
    }

    private String buildActionSignature(MotivationData.TrialContext context, MotivationData.AgentAction action) {
        if (action.sequence >= 0L) {
            return context.trialId + "::seq_" + action.sequence;
        }

        return context.trialId
                + "::"
                + action.actionId.id
                + "::"
                + action.resolvedObjectIndex
                + "::"
                + action.functionalType;
    }

    private void writeCurrentEpochSnapshot(boolean aborted) {
        try {
            if (currentEpoch < 0 || currentEpochResults.isEmpty()) {
                return;
            }

            MotivationEvaluationReport.Summary summary = runner.summarize(
                    architectureName,
                    currentEpoch,
                    aborted,
                    new ArrayList<MotivationEvaluationReport.TrialResult>(currentEpochResults)
            );

            runner.writeEpisodeFiles(summary);

            if (evaluationSummaryMO != null) {
                evaluationSummaryMO.setI(summary);
            }

        } catch (IOException e) {
            System.err.println("[MotivationEvaluationCodelet] write snapshot I/O error: " + e.getMessage());
            e.printStackTrace();

        } catch (Exception e) {
            System.err.println("[MotivationEvaluationCodelet] write snapshot error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void flushCurrentEpoch(boolean aborted) {
        writeCurrentEpochSnapshot(aborted);
        currentEpochResults.clear();
    }

    private void flushIfAllExperimentsDone() {
        if (booleanSignal("motivation_all_done", false)) {
            if (!allExperimentsDoneFlushed) {
                flushCurrentEpoch(false);
                allExperimentsDoneFlushed = true;

                if (debug) {
                    System.out.println("[MotivationEvaluationCodelet] all experiments done; final summary flushed");
                }
            }
        } else {
            allExperimentsDoneFlushed = false;
        }
    }

    private MemoryObject firstInput(String... names) {
        if (names == null) {
            return null;
        }

        for (String name : names) {
            try {
                MemoryObject mo = (MemoryObject) getInput(name);
                if (mo != null) {
                    return mo;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private MemoryObject firstOutput(String... names) {
        if (names == null) {
            return null;
        }

        for (String name : names) {
            try {
                MemoryObject mo = (MemoryObject) getOutput(name);
                if (mo != null) {
                    return mo;
                }
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private void writeJavaStepCsv(
            String status,
            MotivationData.TrialContext context,
            MotivationData.TrialTrace trace
    ) {
        if (!stepLoggingEnabled) {
            return;
        }

        try {
            PrintWriter pw = getStepLogWriter();
            if (pw == null) {
                return;
            }

            StringBuilder sb = new StringBuilder();

            appendCsv(sb, localProcCycle);
            appendCsv(sb, currentEpoch);
            appendCsv(sb, activeExperimentId);
            appendCsv(sb, status);
            appendCsv(sb, context == null ? null : context.trialId);
            appendCsv(sb, context == null ? null : context.phase);
            appendCsv(sb, context == null ? null : context.condition);
            appendCsv(sb, context == null ? null : context.developmentalPhase);
            appendCsv(sb, context == null ? null : context.targetLabel);
            appendCsv(sb, context == null ? null : context.goalLabel);
            appendCsv(sb, context == null ? null : context.devaluedLabel);
            appendCsv(sb, context == null ? null : context.targetRemoved);
            appendCsv(sb, context == null ? null : context.objectBlocked);
            appendCsv(sb, context == null ? null : context.rewardAvailable);
            appendCsv(sb, context == null ? null : context.outcomeDevalued);
            appendCsv(sb, context == null ? null : context.trialComplete);
            appendCsv(sb, trace == null ? null : trace.actions.size());
            appendCsv(sb, responseSeqToSimulator);

            pw.println(sb.toString());
            pw.flush();

        } catch (Exception e) {
            System.err.println("[MotivationEvaluationCodelet] writeJavaStepCsv error: " + e.getMessage());
        }
    }

    private PrintWriter getStepLogWriter() throws IOException {
        if (!stepLoggingEnabled) {
            return null;
        }

        if (stepLogWriter != null) {
            return stepLogWriter;
        }

        File dir = config.outDir == null
                ? new File("motivation_out")
                : config.outDir;

        if (!dir.exists()) {
            dir.mkdirs();
        }

        String prefix = config.filePrefix == null
                ? "motivation"
                : config.filePrefix;

        stepLogFile = new File(
                dir,
                safeFileName(prefix)
                        + "_java_steps_"
                        + safeFileName(architectureName)
                        + ".csv"
        );

        boolean writeHeader = !stepLogFile.exists() || stepLogFile.length() == 0L;
        stepLogWriter = new PrintWriter(new FileWriter(stepLogFile, true));

        if (writeHeader) {
            stepLogWriter.println(
                    "local_proc_cycle,epoch,active_motivation_experiment_id,status,"
                            + "trial_id,phase,condition,developmental_phase,target_label,goal_label,devalued_label,"
                            + "target_removed,object_blocked,reward_available,outcome_devalued,trial_complete,"
                            + "trace_action_count,response_seq_to_simulator"
            );
            stepLogWriter.flush();
        }

        return stepLogWriter;
    }

    private void closeStepLogWriter() {
        if (stepLogWriter == null) {
            return;
        }

        try {
            stepLogWriter.flush();
            stepLogWriter.close();
        } catch (Exception ignored) {
        } finally {
            stepLogWriter = null;
        }
    }

    @Override
    public void stop() {
        flushCurrentEpoch(true);
        closeStepLogWriter();
        super.stop();
    }

    private boolean isAutoExperimentId(String id) {
        if (id == null) {
            return true;
        }

        String s = id.trim().toLowerCase();

        return s.isEmpty()
                || "motivation_experiment".equals(s)
                || "auto".equals(s)
                || "all".equals(s)
                || "all_experiments".equals(s);
    }

    private int parseExperimentId(String id, int fallback) {
        if (id == null) {
            return fallback;
        }

        String s = id.trim().toLowerCase();

        if (s.contains("exp1") || s.contains("persistence")) {
            return MotivationTestRunner.EXP1_PERSISTENCE;
        }

        if (s.contains("exp2") || s.contains("deprivation") || s.contains("satiation")) {
            return MotivationTestRunner.EXP2_DEPRIVATION_SATIATION;
        }

        if (s.contains("exp3") || s.contains("substitution") || s.contains("detour")) {
            return MotivationTestRunner.EXP3_GOAL_SUBSTITUTION;
        }

        if (s.contains("exp4") || s.contains("latent")) {
            return MotivationTestRunner.EXP4_LATENT_LEARNING;
        }

        if (s.contains("exp5") || s.contains("devaluation")) {
            return MotivationTestRunner.EXP5_OUTCOME_DEVALUATION;
        }

        try {
            int numeric = Integer.parseInt(s);
            if (isValidExperimentId(numeric)) {
                return numeric;
            }
        } catch (NumberFormatException ignored) {
        }

        return fallback;
    }

    private boolean isValidExperimentId(int id) {
        return id >= MotivationTestRunner.EXP1_PERSISTENCE
                && id <= MotivationTestRunner.EXP5_OUTCOME_DEVALUATION;
    }

    private void validateExperimentId(int id) {
        if (!isValidExperimentId(id)) {
            throw new IllegalArgumentException(
                    "Motivation experiment id must be between 1 and 5. Received: " + id
            );
        }
    }

    private Integer readIntegerSignal(String signalName) {
        Object value = invokeVisionStringMethod(
                signalName,
                "getIntegerSignal",
                "readIntegerSignal",
                "getIntSignal",
                "readIntSignal"
        );

        if (value instanceof Number) {
            return Integer.valueOf(((Number) value).intValue());
        }

        if (value instanceof String) {
            try {
                return Integer.valueOf(Integer.parseInt(((String) value).trim()));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private Long readLongSignal(String signalName) {
        Integer i = readIntegerSignal(signalName);
        if (i != null) {
            return Long.valueOf(i.longValue());
        }

        Double d = readDoubleSignal(signalName);
        if (d != null) {
            return Long.valueOf(d.longValue());
        }

        return null;
    }

    private Double readDoubleSignal(String signalName) {
        Object value = invokeVisionStringMethod(
                signalName,
                "getFloatSignal",
                "readFloatSignal",
                "getDoubleSignal",
                "readDoubleSignal",
                "getNumberSignal",
                "readNumberSignal"
        );

        if (value instanceof Number) {
            return Double.valueOf(((Number) value).doubleValue());
        }

        if (value instanceof String) {
            try {
                return Double.valueOf(Double.parseDouble(((String) value).trim()));
            } catch (NumberFormatException ignored) {
                return null;
            }
        }

        return null;
    }

    private String readStringSignal(String signalName) {
        Object value = invokeVisionStringMethod(
                signalName,
                "getStringSignal",
                "readStringSignal",
                "getSignal",
                "readSignal"
        );

        return value == null ? null : String.valueOf(value);
    }

    private int intSignal(String signalName, int fallback) {
        Integer v = readIntegerSignal(signalName);
        return v == null ? fallback : v.intValue();
    }

    private long longSignal(String signalName, long fallback) {
        Long v = readLongSignal(signalName);
        return v == null ? fallback : v.longValue();
    }

    private double doubleSignal(String signalName, double fallback) {
        Double v = readDoubleSignal(signalName);
        return v == null ? fallback : v.doubleValue();
    }

    private String stringSignal(String signalName, String fallback) {
        String v = readStringSignal(signalName);
        return v == null || v.trim().isEmpty() ? fallback : v;
    }

    private boolean booleanSignal(String signalName, boolean fallback) {
        Integer v = readIntegerSignal(signalName);
        return v == null ? fallback : v.intValue() != 0;
    }

    private boolean writeIntegerSignal(String signalName, int value) {
        return invokeVisionSetSignalMethod(
                signalName,
                Integer.valueOf(value),
                "setIntegerSignal",
                "writeIntegerSignal",
                "setIntSignal",
                "writeIntSignal"
        );
    }

    private boolean writeStringSignal(String signalName, String value) {
        return invokeVisionSetStringSignalMethod(
                signalName,
                value,
                "setStringSignal",
                "writeStringSignal",
                "setSignal",
                "writeSignal"
        );
    }

    private Object invokeVisionStringMethod(String signalName, String... methodNames) {
        if (vision == null || methodNames == null) {
            return null;
        }

        Class<?> cls = vision.getClass();

        for (String methodName : methodNames) {
            try {
                Method m = cls.getMethod(methodName, String.class);
                return m.invoke(vision, signalName);
            } catch (Exception ignored) {
            }
        }

        return null;
    }

    private boolean invokeVisionSetSignalMethod(String signalName, Object value, String... methodNames) {
        if (vision == null || methodNames == null) {
            return false;
        }

        Class<?> cls = vision.getClass();

        for (String methodName : methodNames) {
            try {
                Method m = cls.getMethod(methodName, String.class, int.class);
                m.invoke(vision, signalName, ((Number) value).intValue());
                return true;
            } catch (Exception ignored) {
            }

            try {
                Method m = cls.getMethod(methodName, String.class, Integer.class);
                m.invoke(vision, signalName, Integer.valueOf(((Number) value).intValue()));
                return true;
            } catch (Exception ignored) {
            }

            try {
                Method m = cls.getMethod(methodName, String.class, Object.class);
                m.invoke(vision, signalName, value);
                return true;
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private boolean invokeVisionSetStringSignalMethod(String signalName, String value, String... methodNames) {
        if (vision == null || methodNames == null) {
            return false;
        }

        Class<?> cls = vision.getClass();

        for (String methodName : methodNames) {
            try {
                Method m = cls.getMethod(methodName, String.class, String.class);
                m.invoke(vision, signalName, value);
                return true;
            } catch (Exception ignored) {
            }

            try {
                Method m = cls.getMethod(methodName, String.class, Object.class);
                m.invoke(vision, signalName, value);
                return true;
            } catch (Exception ignored) {
            }
        }

        return false;
    }

    private void appendCsv(StringBuilder sb, Object value) {
        if (sb.length() > 0) {
            sb.append(',');
        }

        if (value == null) {
            return;
        }

        String s;

        if (value instanceof Double || value instanceof Float) {
            double d = ((Number) value).doubleValue();

            if (Double.isNaN(d) || Double.isInfinite(d)) {
                s = "";
            } else {
                s = String.format(java.util.Locale.US, "%.10f", d);
            }
        } else {
            s = String.valueOf(value);
        }

        if (s.indexOf(',') >= 0 || s.indexOf('"') >= 0 || s.indexOf('\n') >= 0 || s.indexOf('\r') >= 0) {
            sb.append('"').append(s.replace("\"", "\"\"")).append('"');
        } else {
            sb.append(s);
        }
    }

    private String safeFileName(String value) {
        if (value == null || value.trim().isEmpty()) {
            return "unknown";
        }

        return value.trim().replaceAll("[^A-Za-z0-9_.-]", "_");
    }
}

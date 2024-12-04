# Use the base image with VNC and Xubuntu
FROM accetto/xubuntu-vnc-novnc:latest

# Ensure apt-get runs with root privileges
USER root

# Fix potential directory permissions issue before running apt-get
RUN mkdir -p /var/lib/apt/lists/partial

# Set environment variables for Java
ENV JAVA_HOME="/usr/lib/jvm/java-11-openjdk-amd64"
ENV PATH="$JAVA_HOME/bin:$PATH"
ENV DISPLAY=:1
ENV QT_DEBUG_PLUGINS=1
ENV QT_QPA_PLATFORM_PLUGIN_PATH=/usr/lib/x86_64-linux-gnu/qt5/plugins/platforms

# Prepend to LD_LIBRARY_PATH
ENV LD_LIBRARY_PATH=/opt/CoppeliaSim_Pro_V4_1_0_Ubuntu18_04/programming/remoteApiBindings/java/lib/Ubuntu18_04:$LD_LIBRARY_PATH

# Prepend to JAVA_LIBRARY_PATH
ENV JAVA_LIBRARY_PATH=/opt/CoppeliaSim_Pro_V4_1_0_Ubuntu18_04/programming/remoteApiBindings/java/lib/Ubuntu18_04:$JAVA_LIBRARY_PATH

# Install Java, necessary dependencies, and CoppeliaSim
RUN apt-get update && \
    apt-get install -y \
    openjdk-11-jdk \
    wget \
    libx11-6 \
    libglu1-mesa \
    libxi6 \
    libxmu6 \
    libpng16-16 \
    fontconfig \
    libfreetype6 \
    qt5-qmake qtbase5-dev qtbase5-dev-tools \
    libqt5core5a libqt5gui5 libqt5widgets5 \
    libxcb1 libx11-xcb1 libxcb-glx0 \
    libxcb-util1 libxcb-icccm4 libxcb-image0 \
    libxcb-keysyms1 libxcb-render-util0 \
    libxcb-xinerama0 libxcb-randr0 \
    libxcb-shm0 libxcb-shape0 \
     libavcodec-dev \
    libavformat-dev \
    libswscale-dev \
    && rm -rf /var/lib/apt/lists/*


# Install CoppeliaSim (V-REP) in a known location
RUN wget -q https://downloads.coppeliarobotics.com/V4_1_0/CoppeliaSim_Pro_V4_1_0_Ubuntu18_04.tar.xz -O /tmp/coppeliasim.tar.xz && \
    tar -xvf /tmp/coppeliasim.tar.xz -C /opt && \
    rm /tmp/coppeliasim.tar.xz

# Expose the VNC port (5901) and the HTTP port (8080) for noVNC
EXPOSE 5901 8080

# Set the working directory
WORKDIR /root

# Copy the local files into the container
COPY . /root/rl_CSR

# Ensure the group 'headless' exists (if not, create it), and the user 'headless' exists
RUN getent group headless || groupadd -g 1000 headless && \
    id -u headless || true

# Change ownership of /home/headless to the 'headless' user and group
RUN chown -R headless:headless /home/headless


# 1. Start VNC server
#CMD bash -c "../dockerstartup/vnc_startup.sh" 

# 2. Run CoppeliaSim in the background
#RUN /opt/CoppeliaSim_Pro_V4_1_0_Ubuntu18_04/coppeliaSim.sh /root/rl_CSR/scenes/training_obj2.ttt &

# 3. Sleep and wait for CoppeliaSim to load (adjust the sleep time if necessary)
#RUN sleep 10

# 4. Build and run Java application using Gradle
#RUN cd /root/rl_CSR && JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64 ./gradlew --configure-on-demand -x check run
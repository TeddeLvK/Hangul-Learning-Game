UNAME_S := $(shell uname -s)

ifeq ($(UNAME_S),Darwin)
    JAVA_HOME ?= $(shell /usr/libexec/java_home)
    JNI_INC := -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/darwin"
    LIB_EXT := dylib
    SHARED_FLAG := -dynamiclib
    CC := clang
else
    JAVA_HOME ?= $(shell dirname $(shell dirname $(shell readlink -f $(shell which javac))))
    JNI_INC := -I"$(JAVA_HOME)/include" -I"$(JAVA_HOME)/include/linux"
    LIB_EXT := so
    SHARED_FLAG := -shared
    CC := gcc
endif

BUILD_DIR := build
CLASSES_DIR := $(BUILD_DIR)/classes
NATIVE_LIB := $(BUILD_DIR)/libkoreangame.$(LIB_EXT)
C_SRCS := src/native/engine.c src/native/jni_bridge.c
JAVA_SRCS := $(wildcard src/java/com/koreangame/*.java)

.PHONY: all native java run clean

all: native java

native: $(NATIVE_LIB)

$(NATIVE_LIB): $(C_SRCS) src/native/engine.h
	@mkdir -p $(BUILD_DIR)
	$(CC) -std=c11 -O2 -Wall -Wextra -fPIC $(SHARED_FLAG) $(JNI_INC) $(C_SRCS) -lm -o $(NATIVE_LIB)

java:
	@mkdir -p $(CLASSES_DIR)
	javac -encoding UTF-8 -d $(CLASSES_DIR) $(JAVA_SRCS)

run: all
	java -Djava.library.path=$(BUILD_DIR) -cp $(CLASSES_DIR) com.koreangame.Main

clean:
	rm -rf $(BUILD_DIR)

run: all
	java --enable-native-access=ALL-UNNAMED -Djava.library.path=$(BUILD_DIR) -cp $(CLASSES_DIR) com.koreangame.Main

# Copyright © 2012-2013 Jason Ekstrand.
#  
# Permission to use, copy, modify, distribute, and sell this software and its
# documentation for any purpose is hereby granted without fee, provided that
# the above copyright notice appear in all copies and that both that copyright
# notice and this permission notice appear in supporting documentation, and
# that the name of the copyright holders not be used in advertising or
# publicity pertaining to distribution of the software without specific,
# written prior permission.  The copyright holders make no representations
# about the suitability of this software for any purpose.  It is provided "as
# is" without express or implied warranty.
# 
# THE COPYRIGHT HOLDERS DISCLAIM ALL WARRANTIES WITH REGARD TO THIS SOFTWARE,
# INCLUDING ALL IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS, IN NO
# EVENT SHALL THE COPYRIGHT HOLDERS BE LIABLE FOR ANY SPECIAL, INDIRECT OR
# CONSEQUENTIAL DAMAGES OR ANY DAMAGES WHATSOEVER RESULTING FROM LOSS OF USE,
# DATA OR PROFITS, WHETHER IN AN ACTION OF CONTRACT, NEGLIGENCE OR OTHER
# TORTIOUS ACTION, ARISING OUT OF OR IN CONNECTION WITH THE USE OR PERFORMANCE
# OF THIS SOFTWARE.

include files.mk

BUILD_DIR ?= ../build
SRC_DIR ?= src
OBJECT_DIR := $(BUILD_DIR)/objects

ifeq ($(JAVA_HOME),)
JAVA_HOME = $(realpath $(dir $(realpath $(shell which javac)))/../ )
endif
JNI_CFLAGS := -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

PIXMAN_CFLAGS := $(shell pkg-config --cflags pixman-1)
PIXMAN_LDFLAGS := $(shell pkg-config --libs pixman-1)

CFLAGS := -g -fPIC $(JNI_CFLAGS) $(PIXMAN_CFLAGS)
LDFLAGS := $(PIXMAN_LDFLAGS)

CORE_OBJECTS := $(foreach file, $(WHEATLEY_CORE_SRC), \
		$(patsubst $(SRC_DIR)/%.c, $(OBJECT_DIR)/%.o, $(file)))

all: $(BUILD_DIR)/libwheatley-core.so

$(OBJECT_DIR):
	mkdir -p $(OBJECT_DIR)

$(OBJECT_DIR)%.o: $(SRC_DIR)/%.c $(OBJECT_DIR)
	gcc $(CFLAGS) -c $< -o $@

$(BUILD_DIR)/libwheatley-core.so: $(CORE_OBJECTS)
	gcc -shared -o $@ $^ $(LDFLAGS)

.PHONY: clean
clean:
	rm -rf $(OBJECT_DIR)
	rm -f $(BUILD_DIR)/libwheatley-core.so

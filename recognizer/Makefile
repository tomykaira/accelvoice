LIB=build/lib/libaccel_recognizer.so
BIN=build/bin/accel_recognizer
DICT=$(realpath $(dir $(lastword $(MAKEFILE_LIST))))/build/data/accel_voice.dic

all: $(DICT) $(LIB) $(BIN)

MODELDIR=`pkg-config --variable=modeldir pocketsphinx`
DICT_SOURCE=../selector/src/main/resources/java_cmudict.dic
CFLAGS=-g -std=gnu99 -Wall

SRC=accel_recognizer fsg t2p dict callbacks
SRC_FILES=$(SRC:%=src/%.c)
LOCAL_HEADERS=fsg t2p dict callbacks
LOCAL_HEADER_FILES=$(LOCAL_HEADERS:%=src/%.h)
PUBLIC_HEADERS=accel_recognizer
PUBLIC_HEADER_FILES=$(PUBLIC_HEADERS:%=include/%.h)

$(LIB): $(SRC_FILES) $(LOCAL_HEADER_FILES) $(PUBLIC_HEADER_FILES)
	mkdir -p `dirname $(LIB)`
	gcc $(CFLAGS) -o $@ -shared $(SRC_FILES) \
		-DMODELDIR=\"$(MODELDIR)\" \
    -DDICT=\"$(DICT)\" \
		`pkg-config --cflags --libs pocketsphinx sphinxbase gstreamer-0.10` \
		 -lflite_cmu_us_kal -lflite_cmu_time_awb -lflite_cmu_us_kal16 -lflite_cmu_us_awb -lflite_cmu_us_rms -lflite_cmu_us_slt -lflite_usenglish -lflite_cmulex -lflite -lm -lasound

$(BIN): src/accel_recognizer_main.c $(LIB)
	mkdir -p `dirname $(BIN)`
	gcc $(CFLAGS) -o $@ $^ `pkg-config --cflags --libs pocketsphinx sphinxbase gstreamer-0.10`

$(DICT): $(DICT_SOURCE)
	mkdir -p `dirname $(DICT)`
	sed $< -e 's/	[.0-9]*	/        /' > $@

clean:
	\rm -rf build

#ifndef _ACCEL_RECOGNIZER_H_
#define _ACCEL_RECOGNIZER_H_

#define LW 6.5

/* callback types */
typedef void (*cb_vader_start) (long long);
typedef void (*cb_vader_stop) (long long);
typedef void (*cb_recognized) (int);

void start(int argc_p, char *argv_p[]);
void stop();

void register_cb_vader_start(cb_vader_start);
void register_cb_vader_stop(cb_vader_stop);
void register_cb_recognized(cb_recognized);

/*
 * candidates: list of NULL terminated list of upcase words
 * unknown words: NULL terminated list of upcase words
 * @return -1 if error occurred, 0 if successfully prepared
 */
int start_recognition(char **candidates[], char *unknown[]);

/*
 * Stop current recognition and pipeline
 * The result receiver MUST call this.
 */
void abort_recognition();

#endif /* _ACCEL_RECOGNIZER_H_ */

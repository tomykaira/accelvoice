#ifndef _ACCEL_RECOGNIZER_H_
#define _ACCEL_RECOGNIZER_H_

#define LW 6.5

void start(int argc_p, char *argv_p[]);
void stop();

/*
 * candidates: list of NULL terminated list of upcase words
 * unknown words: NULL terminated list of upcase words
 * @return -1 if error occurred, index of candidates if recognize succeeded
 */
int recognize(char **candidates[], char *unknown[]);

#endif /* _ACCEL_RECOGNIZER_H_ */

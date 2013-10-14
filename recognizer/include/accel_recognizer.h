#ifndef _ACCEL_RECOGNIZER_H_
#define _ACCEL_RECOGNIZER_H_

#define LW 6.5

void start(int argc_p, char *argv_p[]);
void stop();

char *recognize(char *candidates[]);

#endif /* _ACCEL_RECOGNIZER_H_ */

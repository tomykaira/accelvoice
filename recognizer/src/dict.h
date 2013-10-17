#ifndef _DICT_H_
#define _DICT_H_

#include "pocketsphinx.h"

void dict_start();
void dict_stop();
int insert_unknown_words(ps_decoder_t *ps, char *unknown[]);

#endif /* _DICT_H_ */

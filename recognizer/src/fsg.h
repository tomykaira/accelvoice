#ifndef _FSG_H_
#define _FSG_H_

#include "pocketsphinx.h"

void fsg_start();
// 0 if succeeded
int add_fsg_model(ps_decoder_t *ps, fsg_model_t *fsg);
fsg_model_t *
build_dynamic_fsg(ps_decoder_t *ps, float lw, char **candidates[], int candidate_length);


#endif /* _FSG_H_ */

#ifndef _CALLBACKS_H_
#define _CALLBACKS_H_

#include <gst/gst.h>

void register_gst_vader_callbacks(GstElement* vader);
void register_gst_asr_callbacks(GstElement* asr);
int find_recognized_index(char const *hyp, char const *uttid);

#endif /* _CALLBACKS_H_ */

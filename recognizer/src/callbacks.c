#include "sphinxbase/err.h"

#include "../include/accel_recognizer.h"
#include "callbacks.h"
#include "logging.h"

static cb_vader_start current_cb_vader_start = NULL;
static cb_vader_stop current_cb_vader_stop = NULL;
static cb_recognized current_cb_recognized = NULL;

void register_cb_vader_start(cb_vader_start new_cb)
{
  current_cb_vader_start = new_cb;
}

void register_cb_vader_stop(cb_vader_stop new_cb)
{
  current_cb_vader_stop = new_cb;
}

void register_cb_recognized(cb_recognized new_cb)
{
  current_cb_recognized = new_cb;
}

/* gst callbacks */
static gboolean
vader_start(const GstElement* asr, GstClockTime ts)
{
  logging_set_fp();
  E_INFO("vader start %lld\n", ts);
  if (current_cb_vader_start != NULL)
    current_cb_vader_start((long long)ts);
  return FALSE;
}

static gboolean
vader_stop(const GstElement* asr, GstClockTime ts)
{
  logging_set_fp();
  E_INFO("vader stop %lld\n", ts);
  if (current_cb_vader_stop != NULL)
    current_cb_vader_stop((long long)ts);
  return FALSE;
}

static gboolean
asr_partial_result(const GstElement* asr, char const *hyp, char const *uttid)
{
  logging_set_fp();
  E_INFO("partial result hyp: %s uttid: %s\n", hyp, uttid);
  return FALSE;
}

static gboolean
asr_result(const GstElement* asr, char const *hyp, char const *uttid)
{
  logging_set_fp();
  int index = find_recognized_index(hyp, uttid);
  E_INFO("result uttid: %s hyp: %s\n", uttid, hyp);
  if (current_cb_recognized != NULL && index >= 0)
    current_cb_recognized(index);
  return FALSE;
}


/* Called in start */
void register_gst_vader_callbacks(GstElement* vader)
{
  g_signal_connect(vader, "vader_start", G_CALLBACK(vader_start), NULL);
  g_signal_connect(vader, "vader_stop", G_CALLBACK(vader_stop), NULL);
}

void register_gst_asr_callbacks(GstElement* asr)
{
  g_signal_connect(asr, "partial_result", G_CALLBACK(asr_partial_result), NULL);
  g_signal_connect(asr, "result", G_CALLBACK(asr_result), NULL);
}

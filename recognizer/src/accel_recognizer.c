#include <stdio.h>
#include <gst/gst.h>
#include <unistd.h>

#include "../include/accel_recognizer.h"
#include "fsg.h"
#include "t2p.h"
#include "dict.h"
#include "callbacks.h"

#define HANDLE_G_ERROR(description) do { \
if (err != NULL) { \
  fprintf(stderr, "%s %s %s:%d\n", description, err->message, __FILE__, __LINE__); \
  g_error_free(err); \
  err = NULL; \
} \
} while(0);

static GValue g_true = G_VALUE_INIT;

static void set_asr_properties(GObject *asr)
{
  GValue g_hmm = G_VALUE_INIT, g_dict = G_VALUE_INIT, g_fsg = G_VALUE_INIT;

  g_value_init(&g_hmm, G_TYPE_STRING);
  g_value_init(&g_fsg, G_TYPE_STRING);
  g_value_init(&g_dict, G_TYPE_STRING);

  g_value_set_static_string(&g_hmm, MODELDIR "/hmm/en_US/hub4wsj_sc_8k");
  g_value_set_static_string(&g_fsg, MODELDIR "/lm/en/tidigits.fsg"); /* temp file to start */
  g_value_set_static_string(&g_dict, DICT);

  g_object_set_property(asr, "hmm", &g_hmm);
  g_object_set_property(asr, "fsg", &g_fsg);
  g_object_set_property(asr, "dict", &g_dict);
  g_object_set_property(asr, "configured", &g_true);

  g_value_unset(&g_hmm);
  g_value_unset(&g_dict);
  g_value_unset(&g_fsg);
}

static ps_decoder_t *ps = NULL;
static GstElement *pipeline = NULL;

void start(int argc_p, char *argv_p[])
{
  GError *err = NULL;
  GstElement* vader = NULL;
  GstElement* asr = NULL;

  g_type_init();

  g_value_init(&g_true, G_TYPE_BOOLEAN);
  g_value_set_boolean(&g_true, TRUE);

  /* Initialize GStreamer */
  gst_init(&argc_p, &argv_p);

  /* Build the pipeline */
  pipeline = gst_parse_launch ("gconfaudiosrc ! audioconvert ! audioresample ! vader name=vad auto-threshold=true ! pocketsphinx name=asr ! fakesink", &err);
  HANDLE_G_ERROR("gst_parse_launch");

  /* set up vader */
  vader = gst_bin_get_by_name((GstBin *)pipeline, "vad");
  register_gst_vader_callbacks(vader);

  /* set up asr */
  asr = gst_bin_get_by_name((GstBin *)pipeline, "asr");
  register_gst_asr_callbacks(asr);
  set_asr_properties((GObject *)asr);

  g_object_get((GObject *)asr, "decoder", &ps, NULL);

  gst_object_unref(vader);
  gst_object_unref(asr);

  t2p_start();
}

void stop()
{
  abort_recognition();
  gst_element_set_state (pipeline, GST_STATE_NULL);

  t2p_stop();

  /* Free resources */
  g_value_unset(&g_true);
  gst_object_unref (pipeline);
}


static int count_candidates(char **candidates[])
{
  int length = 0;
  while (candidates[length] != NULL)
    length += 1;
  return length;
}

static char *join(char **words)
{
  char *result = ckd_calloc(1024, sizeof(char));
  int ptr = 0;
  while (words[ptr] != NULL) {
    if (result[0] == '\0')
      strcpy(result, words[ptr]);
    else
      sprintf(result, "%s %s", result, words[ptr]);
    ptr++;
  }
  return result;
}

static void vader_force_silent()
{
  GstElement* vader = NULL;

  vader = gst_bin_get_by_name((GstBin *)pipeline, "vad");

  g_object_set_property((GObject *)vader, "silent", &g_true);
}

static hash_table_t *index_map;

int start_recognition(char **candidates[], char *unknown[])
{
  fsg_model_t *fsg;
  fsg_set_t* fsgs;
  int candidate_length;

  vader_force_silent();

  candidate_length = count_candidates(candidates);

  if (insert_unknown_words(ps, unknown) < 0)
    return -1;

  fsg = build_dynamic_fsg(ps, LW, candidates, candidate_length);
  fsgs = ps_get_fsgset(ps);
  if (add_fsg_model(ps, fsg) != 0)
    return -1;
  fsg_set_select(fsgs, fsg_model_name(fsg));
  ps_update_fsgset(ps);
  gst_element_set_state (pipeline, GST_STATE_PLAYING);

  if (index_map != NULL) {
    hash_table_free(index_map);
    index_map = NULL;
  }

  index_map = hash_table_new(candidate_length * 2, HASH_CASE_YES);
  for (int i = 0; i < candidate_length; ++i) {
    (void)hash_table_enter_int32(index_map, join(candidates[i]), i);
  }

  return 0;
}

void abort_recognition()
{
  vader_force_silent();
  gst_element_set_state (pipeline, GST_STATE_PAUSED);

  if (index_map != NULL) {
    hash_table_free(index_map);
    index_map = NULL;
  }
}

int find_recognized_index(char const *hyp, char const *uttid)
{
  int result_index;

  if (index_map == NULL)
    return -1;

  if (hash_table_lookup_int32(index_map, hyp, &result_index) == - 1)
    return -1;

  abort_recognition();
  return result_index;
}

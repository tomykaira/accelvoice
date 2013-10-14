#include <string.h>
#include <unistd.h>
#include <ctype.h>

#include <flite/flite.h>
#include "t2p.h"

/* Forward declarations of functions in header files of flite's lang directory */
/* #include "../lang/usenglish/usenglish.h" */
/* #include "../lang/cmulex/cmu_lex.h" */
cst_lexicon *cmu_lex_init(void);
void usenglish_init(cst_voice *v);

/*
  From flite-1.4  main/t2p_main.c
  Original author is Alan W Black (awb@cs.cmu.edu).
  Licensed by Carnegie Mellon University
 */
static cst_voice *cmu_us_no_wave = NULL;

static cst_utterance *no_wave_synth(cst_utterance *u)
{
  return u;
}

static cst_voice *register_cmu_us_no_wave(const char *voxdir)
{
  cst_voice *v = new_voice();
  cst_lexicon *lex;

  v->name = "no_wave_voice";

  /* Set up basic values for synthesizing with this voice */
  usenglish_init(v);
  feat_set_string(v->features,"name","cmu_us_no_wave");

  /* Lexicon */
  lex = cmu_lex_init();
  feat_set(v->features,"lexicon",lexicon_val(lex));

  /* Intonation */
  feat_set_float(v->features,"int_f0_target_mean",95.0);
  feat_set_float(v->features,"int_f0_target_stddev",11.0);

  feat_set_float(v->features,"duration_stretch",1.1);

  /* Post lexical rules */
  feat_set(v->features,"postlex_func",uttfunc_val(lex->postlex));

  /* Waveform synthesis: diphone_synth */
  feat_set(v->features,"wave_synth_func",uttfunc_val(&no_wave_synth));

  cmu_us_no_wave = v;

  return cmu_us_no_wave;
}

static char * normalize_phone(const char *orig)
{
  const int length = strlen(orig);
  char * normalized = calloc(length + 1, sizeof(char));
  if (strcmp(orig, "ax") == 0)
    orig = "ah";

  for (int i = length - 1; i >= 0; --i) {
    normalized[i] = toupper(orig[i]);
  }
  normalized[length] = 0;
  return normalized;
}

static cst_voice *v = NULL;

void t2p_start()
{
  flite_init();
  v = register_cmu_us_no_wave(NULL);
}

void t2p_stop()
{
  delete_voice(v);
  v = NULL;
}

/* Author: tomykaira  Year: 2013 */
char *synthesize_phones(const char *text)
{
  cst_utterance *u = NULL;
  cst_item *s = NULL;
  const char *name;
  char *normalized = NULL;
  uint maxlen = 128;
  char *result = calloc(maxlen, sizeof(char));
  u = flite_synth_text(text,v);

  for (s=relation_head(utt_relation(u,"Segment"));
       s;
       s = item_next(s)) {
    name = item_feat_string(s,"name");

    if (strcmp(name, "pau") == 0)
      continue;

    normalized = normalize_phone(name);

    if (strlen(result) + 1 + strlen(normalized) > maxlen) {
      result = realloc(result, maxlen*2);
      maxlen *= 2;
    }
    if (result[0] == 0) {
      strcpy(result, normalized);
    } else {
      strcat(result, " ");
      strcat(result, normalized);
    }

    free(normalized);
    normalized = NULL;
  }

  delete_utterance(u);
  return result;
}

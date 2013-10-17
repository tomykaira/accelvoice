#include "fsg.h"

#include <stdint.h>
#include <stdio.h>
#include <assert.h>
#include <sphinxbase/err.h>

int
add_fsg_model(ps_decoder_t *ps, fsg_model_t *fsg)
{
  fsg_set_t *set;

  assert(fsg != NULL);
  if ((set = ps_get_fsgset(ps)) == NULL) {
    E_FATAL("Failed to get fsgset\n");
    return -1;
  }
  if (fsg_set_add(set, fsg_model_name(fsg), fsg) != fsg) {
    fsg_model_free(fsg);
    E_WARN("Failed to add fsg %x\n", (intptr_t) fsg);
    return -1;
  }
  return 0;
}

#define NULL_TRANS_ADD(i, j, logp) do { \
if (fsg_model_null_trans_add(fsg, i, j, logp)) { \
    nulls = glist_add_ptr(nulls, fsg_model_null_trans(fsg, i, j)); \
} \
} while(0)

static int name_id = 0;

void fsg_start()
{
  name_id = 0;
}

static void next_name(char *buf)
{
  sprintf(buf, "gram_%d", name_id++);
}

static void fsg_dump_model(fsg_model_t *fsg)
{
  char filename[256];
  FILE *fp;
  sprintf(filename, "/tmp/%s", fsg_model_name(fsg));
  fp = fopen(filename, "w");
  fsg_model_write(fsg, fp);
  fclose(fp);
}

static int count_states(char **candidates[], int candidate_length)
{
  int count = 2;
  for (int i = 0; i < candidate_length; ++i) {
    int j = 0;
    count++;
    while (candidates[i][j] != NULL) {
      count++; j++;
    }
  }
  return count;
}

fsg_model_t *
build_dynamic_fsg(ps_decoder_t *ps, float lw, char **candidates[], int candidate_length)
{
  int32 logmath_nth, logmath_1;
  fsg_model_t *fsg;
  hash_table_t *vocab;
  int32 lastwid = 0;
  hash_iter_t *itor;
  glist_t nulls = NULL;
  char name[16];

  int states = count_states(candidates, candidate_length);

  vocab = hash_table_new(states, FALSE);

  /*
          (2)  - get -> (3)
        /                   \
    (0) - (4)  - set -> (5) - (1)
        \                   /
          (6)  - add -> (7)
  */
  next_name(name);
  fsg = fsg_model_init(name, ps_get_logmath(ps), lw, states);
  fsg->start_state = 0;
  fsg->final_state = 1;

  logmath_nth = (int32) (logmath_log(fsg->lmath, 1/(float)candidate_length) * fsg->lw);
  logmath_1 = (int32) (logmath_log(fsg->lmath, 1.0) * fsg->lw);

  int state_id = 2;
  for (int i = 0; i < candidate_length; ++i) {
    int j = 0;
    NULL_TRANS_ADD(fsg->start_state, state_id, logmath_nth);
    while (candidates[i][j] != NULL) {
      int wid = hash_table_enter_int32(vocab, ckd_salloc(candidates[i][j]), lastwid);
      fsg_model_trans_add(fsg, state_id, state_id + 1, logmath_1, wid);
      ++state_id;
      if (wid == lastwid)
        ++lastwid;
      ++j;
    }
    NULL_TRANS_ADD(state_id, fsg->final_state, logmath_nth);
    ++state_id;
  }

  fsg->n_word = hash_table_inuse(vocab);
  fsg->n_word_alloc = fsg->n_word + 10;
  fsg->vocab = (char **)ckd_calloc(fsg->n_word_alloc, sizeof(*fsg->vocab));
  for (itor = hash_table_iter(vocab); itor;
       itor = hash_table_iter_next(itor)) {
    char const *word = hash_entry_key(itor->ent);
    int32 wid = (int32) (long) hash_entry_val(itor->ent);
    fsg->vocab[wid] = (char *) word;
  }
  hash_table_free(vocab);

  nulls = fsg_model_null_trans_closure(fsg, nulls);
  glist_free(nulls);

  fsg_dump_model(fsg);

  return fsg;
}

#!/bin/bash

######## NER

# Human only (Max vote)
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.ner.runnables.MaxVoteNoMLRealData

# ML only
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.ner.runnables.NERMLOnlyRealData

# Threshold
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.ner.runnables.NERThresholdGameplayerRealData

# MCTS
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.ner.runnables.NERMCTSGameplayerRealData

######## Sentiment

# Human only (Max vote)
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.sentiment.runnables.MaxVote

# ML only
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.sentiment.runnables.SentimentMLOnly

# Threshold
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.sentiment.runnables.SentimentThresholdGameplayer

# MCTS - no embeddings
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.sentiment.runnables.SentimentMCTSNoEmbeddingsGameplayer

# MCTS
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.sentiment.runnables.SentimentMCTSGameplayer

######## Faces

# Human only (Max vote)
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.faces.runnables.MaxVote

# ML only
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.faces.runnables.FacesMLOnly

# Threshold
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.faces.runnables.FacesThresholdGameplay

# MCTS
java -cp target/lense-examples-1.0-SNAPSHOT-jar-with-dependencies.jar com.github.keenon.lense.examples.faces.runnables.FacesMCTSGameplay

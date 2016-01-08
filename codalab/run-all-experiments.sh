#!/bin/bash

# Package monolithic jar
# mvn clean compile assembly:single

FLAGS="-Xmx32G"

######## NER

# Human only (Max vote)
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.ner.runnables.MaxVoteNoMLRealData ner-src 2> ner-dst/max-vote-results.txt

# ML only
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.ner.runnables.NERMLOnlyRealData ner-src ner-dst

# Threshold
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.ner.runnables.NERThresholdGameplayerRealData ner-src ner-dst

# MCTS
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.ner.runnables.NERMCTSGameplayerRealData ner-src ner-dst

######## Sentiment

# Human only (Max vote)
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.sentiment.runnables.MaxVote sentiment-src 2> sentiment-dst/max-vote-results.txt

# ML only
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.sentiment.runnables.SentimentMLOnly sentiment-src sentiment-dst

# Threshold
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.sentiment.runnables.SentimentThresholdGameplayer sentiment-src sentiment-dst

# MCTS - no embeddings
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.sentiment.runnables.SentimentMCTSNoEmbeddingsGameplayer sentiment-src sentiment-dst

# MCTS
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.sentiment.runnables.SentimentMCTSGameplayer sentiment-src sentiment-dst

######## Faces

# Human only (Max vote)
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.faces.runnables.MaxVote person_recognition-src 2> person_recognition-dst/max-vote-results.txt

# ML only
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.faces.runnables.FacesMLOnly person_recognition-src person_recognition-dst

# Threshold
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.faces.runnables.FacesThresholdGameplay person_recognition-src person_recognition-dst

# MCTS
java $FLAGS -cp lense.jar com.github.keenon.lense.examples.faces.runnables.FacesMCTSGameplay person_recognition-src person_recognition-dst


rm(list = ls())
#setwd("Desktop/")


### source('SpearmanQualitySecurity.R')

#G3 = read.csv("data/all.csv", header = T)
G3 = read.csv("/Users/dxkvse/Desktop/SecurityQuality.csv", header = T)


############################## Spearman Analysis ###############################
### Make sure the values are all normalized by LOC



### Be careful as the titles are case sensative
### Make sure that there are no empty values. Replace with 0. Empty values will lead to NaN (or something similar)

## Opriv, JLint
 print(paste("OPriv:", "Jlint:", cor(G3$JLintResult,G3$OPrivCount,method="spearman"), "K:",cor(G3$JLintResult,G3$OPrivCount,method="kendall")))


### Opriv, CSDefect
 print(paste("OPriv:", "CSDefect:", cor(G3$CSDefect,G3$OPrivCount,method="spearman"), "K:",cor(G3$CSDefect,G3$OPrivCount,method="kendall")))


#### FuzzyRisk, CSDefect
 print(paste("FuzzyRisk:", "CSDefect:", cor(G3$CSDefect,G3$FuzzyRisk,method="spearman"), "K:",cor(G3$CSDefect,G3$FuzzyRisk,method="kendall")))

### FuzzyRisk, Jlint
 print(paste("FuzzyRisk:", "JLintResult:", cor(G3$JLintResult,G3$FuzzyRisk,method="spearman"), "K:",cor(G3$JLintResult,G3$FuzzyRisk,method="kendall")))


### JLint, CSDefect
 print(paste("CSDefect:", "JLintResult:", cor(G3$JLintResult,G3$CSDefect,method="spearman"), "K:",cor(G3$JLintResult,G3$CSDefect,method="kendall")))



#### Normalize by LOC
print("NormalizeLOC")



## Opriv, JLint
 print(paste("OPriv:", "JlintLOC:", cor(G3$jlintLOC,G3$OPrivCount,method="spearman"), "K:",cor(G3$JLintResult,G3$OPrivCount,method="kendall")))


### Opriv, CSDefect
 print(paste("OPriv:", "CSLOC:", cor(G3$CSLOC,G3$OPrivCount,method="spearman"), "K:",cor(G3$CSLOC,G3$OPrivCount,method="kendall")))


#### FuzzyRisk, CSDefect
 print(paste("FuzzyRisk:", "CSLOC:", cor(G3$CSLOC,G3$FuzzyRisk,method="spearman"), "K:",cor(G3$CSLOC,G3$FuzzyRisk,method="kendall")))

### FuzzyRisk, Jlint
 print(paste("FuzzyRisk:", "jlintLOC:", cor(G3$jlintLOC,G3$FuzzyRisk,method="spearman"), "K:",cor(G3$jlintLOC,G3$FuzzyRisk,method="kendall")))


### JLint, CSDefect
 print(paste("CSDefect:", "JLintResult:", cor(G3$jlintLOC,G3$CSLOC,method="spearman"), "K:",cor(G3$jlintLOC,G3$CSLOC,method="kendall")))



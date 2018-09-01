rm(list = ls())
#setwd("Desktop/data/")

#source('UserRatingsCorrelation.R')

G3 = read.csv("UserRatings.csv", header = T)


############################## Spearman Analysis ###############################

### Spearman

# print(paste("OPriv:", "SP:",cor(G3$Ownership,G3$PermissionChange,method="spearman"), "K:",cor(G3$Ownership,G3$PermissionChange,method="kendall")))


## Spearman
print(cor(G3$AvgRating,G3$AvgManifestCommtterratio,method="spearman"))

## Kendell
print(cor(G3$AvgRating,G3$AvgManifestCommtterratio,method="kendall"))

## Pearson
print(cor(G3$AvgRating,G3$AvgManifestCommtterratio,method="pearson"))
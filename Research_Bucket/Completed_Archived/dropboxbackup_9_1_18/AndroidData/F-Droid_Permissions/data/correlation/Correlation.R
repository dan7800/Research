rm(list = ls())
#setwd("Desktop/data/")

#source('Spearman.R')

#G1 = read.csv("data/0-3.csv", header = T)
#G2 = read.csv("data/3-5.csv", header = T)
G3 = read.csv("data/all.csv", header = T)


############################## Spearman Analysis ###############################

### Spearman

# print(paste("OPriv:", "SP:",cor(G3$Ownership,G3$PermissionChange,method="spearman"), "K:",cor(G3$Ownership,G3$PermissionChange,method="kendall")))


## Spearman
print(cor(G3$Ownership,G3$PermissionChange,method="spearman"))

## Kendell
print(cor(G3$Ownership,G3$PermissionChange,method="kendall"))

## Pearson
print(cor(G3$Ownership,G3$PermissionChange,method="pearson"))
#	Change current dir to be location of this file
#	source('Correlations.R')


rm(list = ls())


#G1 = read.csv("data/AllData.csv", header = T)
#G1 = read.csv("data/Education.csv", header = T)
#G1 = read.csv("data/Entertainment.csv", header = T)
#G1 = read.csv("data/Tools.csv", header = T)
#G1 = read.csv("data/Personalization.csv", header = T)
G1 = read.csv("data/Puzzle.csv", header = T)



print('**** Opriv ****') 

########## OPriv ##########
## Spearman
	print(cor(G1$UserRating,G1$OPrivCount,method="spearman"))

## Kendell
	print(cor(G1$UserRating,G1$OPrivCount,method="kendall"))

## Pearson
	print(cor(G1$UserRating,G1$OPrivCount,method="pearson"))



print('**** Upriv ****') 
########## UPriv ##########
## Spearman
	print(cor(G1$UserRating,G1$UPrivCount,method="spearman"))

## Kendell
	print(cor(G1$UserRating,G1$UPrivCount,method="kendall"))

## Pearson
	print(cor(G1$UserRating,G1$UPrivCount,method="pearson"))


print('**** Permissions ****') 
########## Permissions ##########
## Spearman
	print(cor(G1$UserRating,G1$PermissionCount,method="spearman"))

## Kendell
	print(cor(G1$UserRating,G1$PermissionCount,method="kendall"))

## Pearson
	print(cor(G1$UserRating,G1$PermissionCount,method="pearson"))



print('**** AndroRisk ****') 
########## RuzzyRisk ##########
## Spearman
	print(cor(G1$UserRating,G1$AndroRisk,method="spearman"))

## Kendell
	print(cor(G1$UserRating,G1$AndroRisk,method="kendall"))

## Pearson
	print(cor(G1$UserRating,G1$AndroRisk,method="pearson"))
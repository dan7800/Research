rm(list = ls())
#setwd("Desktop/data/")


# source('SpearmanUserRatings.R')

G3 = read.csv("data/all.csv", header = T)


#### Compare user rating with all of these


#print("-- OPriv --")
print(paste("OPriv:", "SP:",cor(G3$UserRatings,G3$OPrivCount,method="spearman"), "K:",cor(G3$UserRatings,G3$OPrivCount,method="kendall"),
"P:",cor(G3$UserRatings,G3$OPrivCount,method="pearson"))
)



#print("-- UPriv --")
print(paste("UPriv:", "SP:",cor(G3$UserRatings,G3$UPrivCount,method="spearman"), "K:",cor(G3$UserRatings,G3$UPrivCount,method="kendall"),
"P:",cor(G3$UserRatings,G3$UPrivCount,method="pearson"))
)


#print("-- ARisk --")
print(paste("FRisk:", "SP:",cor(G3$UserRatings,G3$FuzzyRisk,method="spearman"), "K:",cor(G3$UserRatings,G3$FuzzyRisk,method="kendall"),
"P:",cor(G3$UserRatings,G3$FuzzyRisk,method="pearson"))
)


#print("-- PrivCount --")
print(paste("PermCount:", "SP:",cor(G3$UserRatings,G3$PermissionCount,method="spearman"), "K:",cor(G3$UserRatings,G3$PermissionCount,method="kendall"),
"P:",cor(G3$UserRatings,G3$PermissionCount,method="pearson"))
)


### CS
print(paste("CS:", "SP:",cor(G3$UserRatings,G3$DefectCount,method="spearman"), "K:",cor(G3$UserRatings,G3$DefectCount,method="kendall"),
"P:",cor(G3$UserRatings,G3$DefectCount,method="pearson"))
)



### Jlint
print(paste("Jlint:", "SP:",cor(G3$UserRatings,G3$jlint,method="spearman"), "K:",cor(G3$UserRatings,G3$jlint,method="kendall"),
"P:",cor(G3$UserRatings,G3$jlint,method="pearson"))
)




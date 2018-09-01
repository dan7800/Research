rm(list = ls())

### Remember to change this below
#setwd("/Users/dxkvse/Desktop/Rdata/") ### Obviously only for a mac


G1 = read.csv("data/all_No_LOC.csv", header = T)
G2 = read.csv("data/all-tools_No_LOC.csv", header = T)
G3 = read.csv("data/all-Puzzle_No_LOC.csv", header = T)
G4 = read.csv("data/all-Entertainment_No_LOC.csv", header = T)
G5 = read.csv("data/all-Education_No_LOC.csv", header = T)

#all_No_LOC
#all-tools_No_LOC
#all-Puzzle_No_LOC
#all-Entertainment_No_LOC
#all-Education_No_LOC





### Must remove LOC.

n = ncol(G1)
name_vec = names(G1)
G1[,5:7] = 1000*(G1[,5:7]/G1[,8])
G1[,9:n] = 1000*(G1[,9:n]/G1[,8])

#print(paste("OPriv:", "SP:",cor(G1$UserRatings,G1$OPrivCount,method="spearman")))

print("****************All*****************")

#print("-- OPriv --")
print(paste("OPriv:", "SP:",cor(G1$UserRatings,G1$OPrivCount,method="spearman"), "K:",cor(G1$UserRatings,G1$OPrivCount,method="kendall"),
"P:",cor(G1$UserRatings,G1$OPrivCount,method="pearson"))
)



#print("-- UPriv --")
print(paste("UPriv:", "SP:",cor(G1$UserRatings,G1$UPrivCount,method="spearman"), "K:",cor(G1$UserRatings,G1$UPrivCount,method="kendall"),
"P:",cor(G1$UserRatings,G1$UPrivCount,method="pearson"))
)


#print("-- ARisk --")
print(paste("FRisk:", "SP:",cor(G1$UserRatings,G1$FuzzyRisk,method="spearman"), "K:",cor(G1$UserRatings,G1$FuzzyRisk,method="kendall"),
"P:",cor(G1$UserRatings,G1$FuzzyRisk,method="pearson"))
)


#print("-- PrivCount --")
print(paste("PermCount:", "SP:",cor(G1$UserRatings,G1$PermissionCount,method="spearman"), "K:",cor(G1$UserRatings,G1$PermissionCount,method="kendall"),
"P:",cor(G1$UserRatings,G1$PermissionCount,method="pearson"))
)


######################################

print("****************Tools**************")
n = ncol(G2)
name_vec = names(G2)
G2[,5:7] = 1000*(G2[,5:7]/G2[,8])
G2[,9:n] = 1000*(G2[,9:n]/G2[,8])

#print(paste("OPriv:", "SP:",cor(G2$UserRatings,G2$OPrivCount,method="spearman")))

#print("-- OPriv --")
print(paste("OPriv:", "SP:",cor(G2$UserRatings,G2$OPrivCount,method="spearman"), "K:",cor(G2$UserRatings,G2$OPrivCount,method="kendall"),
"P:",cor(G2$UserRatings,G2$OPrivCount,method="pearson"))
)



#print("-- UPriv --")
print(paste("UPriv:", "SP:",cor(G2$UserRatings,G2$UPrivCount,method="spearman"), "K:",cor(G2$UserRatings,G2$UPrivCount,method="kendall"),
"P:",cor(G2$UserRatings,G2$UPrivCount,method="pearson"))
)


#print("-- ARisk --")
print(paste("FRisk:", "SP:",cor(G2$UserRatings,G2$FuzzyRisk,method="spearman"), "K:",cor(G2$UserRatings,G2$FuzzyRisk,method="kendall"),
"P:",cor(G2$UserRatings,G2$FuzzyRisk,method="pearson"))
)


#print("-- PrivCount --")
print(paste("PermCount:", "SP:",cor(G2$UserRatings,G2$PermissionCount,method="spearman"), "K:",cor(G2$UserRatings,G2$PermissionCount,method="kendall"),
"P:",cor(G2$UserRatings,G2$PermissionCount,method="pearson"))
)


######################################

print("****************Puzzle**************")


n = ncol(G3)
name_vec = names(G3)
G3[,5:7] = 1000*(G3[,5:7]/G3[,8])
G3[,9:n] = 1000*(G3[,9:n]/G3[,8])

#print(paste("OPriv:", "SP:",cor(G3$UserRatings,G3$OPrivCount,method="spearman")))

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


######################################






print("****************Entertainment**************")



n = ncol(G4)
name_vec = names(G4)
G4[,5:7] = 1000*(G4[,5:7]/G4[,8])
G4[,9:n] = 1000*(G4[,9:n]/G4[,8])

#print(paste("OPriv:", "SP:",cor(G4$UserRatings,G4$OPrivCount,method="spearman")))

#print("-- OPriv --")
print(paste("OPriv:", "SP:",cor(G4$UserRatings,G4$OPrivCount,method="spearman"), "K:",cor(G4$UserRatings,G4$OPrivCount,method="kendall"),
"P:",cor(G4$UserRatings,G4$OPrivCount,method="pearson"))
)



#print("-- UPriv --")
print(paste("UPriv:", "SP:",cor(G4$UserRatings,G4$UPrivCount,method="spearman"), "K:",cor(G4$UserRatings,G4$UPrivCount,method="kendall"),
"P:",cor(G4$UserRatings,G4$UPrivCount,method="pearson"))
)


#print("-- ARisk --")
print(paste("FRisk:", "SP:",cor(G4$UserRatings,G4$FuzzyRisk,method="spearman"), "K:",cor(G4$UserRatings,G4$FuzzyRisk,method="kendall"),
"P:",cor(G4$UserRatings,G4$FuzzyRisk,method="pearson"))
)


#print("-- PrivCount --")
print(paste("PermCount:", "SP:",cor(G4$UserRatings,G4$PermissionCount,method="spearman"), "K:",cor(G4$UserRatings,G4$PermissionCount,method="kendall"),
"P:",cor(G4$UserRatings,G4$PermissionCount,method="pearson"))
)


######################################




print("****************Education**************")



n = ncol(G5)
name_vec = names(G5)
G5[,5:7] = 1000*(G5[,5:7]/G5[,8])
G5[,9:n] = 1000*(G5[,9:n]/G5[,8])

#print(paste("OPriv:", "SP:",cor(G5$UserRatings,G5$OPrivCount,method="spearman")))

#print("-- OPriv --")
print(paste("OPriv:", "SP:",cor(G5$UserRatings,G5$OPrivCount,method="spearman"), "K:",cor(G5$UserRatings,G5$OPrivCount,method="kendall"),
"P:",cor(G5$UserRatings,G5$OPrivCount,method="pearson"))
)



#print("-- UPriv --")
print(paste("UPriv:", "SP:",cor(G5$UserRatings,G5$UPrivCount,method="spearman"), "K:",cor(G5$UserRatings,G5$UPrivCount,method="kendall"),
"P:",cor(G5$UserRatings,G5$UPrivCount,method="pearson"))
)


#print("-- ARisk --")
print(paste("FRisk:", "SP:",cor(G5$UserRatings,G5$FuzzyRisk,method="spearman"), "K:",cor(G5$UserRatings,G5$FuzzyRisk,method="kendall"),
"P:",cor(G5$UserRatings,G5$FuzzyRisk,method="pearson"))
)


#print("-- PrivCount --")
print(paste("PermCount:", "SP:",cor(G5$UserRatings,G5$PermissionCount,method="spearman"), "K:",cor(G5$UserRatings,G5$PermissionCount,method="kendall"),
"P:",cor(G5$UserRatings,G5$PermissionCount,method="pearson"))
)


######################################





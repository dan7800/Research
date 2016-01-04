


#### Find different metrics for apps across genres
	select count(ai.rowID) as AppCount, ai.Genre, round(avg(FuzzyRiskValue),2) as FuzzyRisk, round(avg(JavaFiles),2) as JavaFiles, round(avg(JlintResult),2) as JLint, round(avg(DefectCount),2) as DefectCount, round(avg(LOC),2) as LOC, round(avg(simcad_clonefragment),2) as CloneFragment, round(avg(simcad_cloneGroup),2) as CloneGroup

	, round(avg(JlintResult)/avg(LOC) * 10000,1) as "Jlint/LOC"
	, round(avg(DefectCount)/avg(LOC) * 10000,1) as "Defect/LOC"
	, round(avg(OPrivCount),1) as OPriv
	, round(avg(PrivCount),1) as PrivCount
	, round(round(avg(PrivCount),1)/round(avg(OPrivCount),1),2) as OPrivRatio

	from apkInformation ai
	inner join toolResults tr on tr.apkID = ai.rowID

	left outer join (select count (permissionID) as OPrivCount, apkID from overprivilege o group by apkID) overprivCount on overprivCount.apkID = ai.rowID

	inner join (select count (privID) as PrivCount, rowID from apkParser_privs_join apj group by rowID) permissionCount on permissionCount.rowID = ai.rowID

	where ai.lowerdownloads >=10000
	group by ai.genre
	order by ai.genre


#####

#### Find information to compare Benign apps to GooglePlay ####

	select count(ai.rowID) as AppCount, round(avg(FuzzyRiskValue),2) as FuzzyRisk, round(avg(JavaFiles),2) as JavaFiles, round(avg(JlintResult),2) as JLint, round(avg(DefectCount),2) as DefectCount, round(avg(LOC),2) as LOC, round(avg(simcad_clonefragment),2) as CloneFragment, round(avg(simcad_cloneGroup),2) as CloneGroup 

	, round(avg(JlintResult)/avg(LOC) * 10000,1) as "Jlint/LOC"
	, round(avg(DefectCount)/avg(LOC) * 10000,1) as "Defect/LOC"
	, round(avg(OPrivCount),1) as OPriv
	, round(avg(UPrivCount),1) as UPriv
	, round(avg(PrivCount),1) as PrivCount
	
	, round(round(avg(PrivCount),1)/round(avg(OPrivCount),1),2) as OPrivRatio
	, round(round(avg(PrivCount),1)/round(avg(UPrivCount),1),2) as UPrivRatio


	from apkInformation ai
	inner join toolResults tr on tr.apkID = ai.rowID

	left outer join (select count (permissionID) as OPrivCount, apkID from overprivilege o group by apkID) overprivCount on overprivCount.apkID = ai.rowID

	left outer join (select count (permissionID) as UPrivCount, apkID from underprivilege u group by apkID) underprivCount on underprivCount.apkID = ai.rowID


	%% Think this should be a "left outer"
	%%-- No. This is because no legitimate apps should have no permissions
	inner join (select count (privID) as PrivCount, rowID from apkParser_privs_join apj group by rowID) permissionCount on permissionCount.rowID = ai.rowID


	### Simply return all info #####
	select ai.rowID,  FuzzyRiskValue  as FuzzyRisk,  JavaFiles  as JavaFiles,  JlintResult  as JLint,  DefectCount  as DefectCount,  LOC  as LOC,  simcad_clonefragment  as CloneFragment,  simcad_cloneGroup  as CloneGroup 

	,  (JlintResult/LOC) * 10000 as "Jlint/LOC"
	,  (DefectCount/LOC) * 10000 as "Defect/LOC"
	,  OPrivCount as OPriv
	,  UPrivCount as UPriv
	,  PrivCount as PrivCount
	
	, round((PrivCount/ OPrivCount),1)  as OPrivRatio
	, round((PrivCount/ UPrivCount),1)  as UPrivRatio

	from apkInformation ai
	inner join toolResults tr on tr.apkID = ai.rowID
	left outer join (select count (permissionID) as OPrivCount, apkID from overprivilege o group by apkID) overprivCount on overprivCount.apkID = ai.rowID
	left outer join (select count (permissionID) as UPrivCount, apkID from underprivilege u group by apkID) underprivCount on underprivCount.apkID = ai.rowID
	inner join (select count (privID) as PrivCount, rowID from apkParser_privs_join apj group by rowID) permissionCount on permissionCount.rowID = ai.rowID

	#### Return all the info, but from GP

	select ai.rowID,  FuzzyRiskValue  as FuzzyRisk,  JavaFiles  as JavaFiles,  JlintResult  as JLint,  DefectCount  as DefectCount,  LOC  as LOC,  simcad_clonefragment  as CloneFragment,  simcad_cloneGroup  as CloneGroup 

	,  (JlintResult/LOC) * 10000 as "Jlint/LOC"
	,  (DefectCount/LOC) * 10000 as "Defect/LOC"
	,  OPrivCount as OPriv
	,  UPrivCount as UPriv
	,  PrivCount as PrivCount
	
	, round((PrivCount/ OPrivCount),1)  as OPrivRatio
	, round((PrivCount/ UPrivCount),1)  as UPrivRatio

	from apkInformation ai
	inner join toolResults tr on tr.apkID = ai.rowID

	left outer join (select count (permissionID) as OPrivCount, apkID from overprivilege o group by apkID) overprivCount on overprivCount.apkID = ai.rowID
	left outer join (select count (permissionID) as UPrivCount, apkID from underprivilege u group by apkID) underprivCount on underprivCount.apkID = ai.rowID
	inner join (select count (privID) as PrivCount, rowID from apkParser_privs_join apj group by rowID) permissionCount on permissionCount.rowID = ai.rowID

	where ai.lowerdownloads >=10000
	and javaFiles >0
	




	
	### Find information about a specific app

	select  ai.name, ai.rowID as RowID, FuzzyRiskValue as FuzzyRisk,  JavaFiles as JavaFiles,  JlintResult as JLint, DefectCount as DefectCount,  LOC as LOC,  simcad_clonefragment CloneFragment, 
simcad_cloneGroup as CloneGroup 

	, (JlintResult/LOC) * 10000 as "Jlint/LOC"
	, (DefectCount/LOC) * 10000 as "Defect/LOC"
	, OPrivCount as OPriv
	, UPrivCount as UPriv
	, PrivCount as PrivCount
	
	, (PrivCount/OPrivCount) as OPrivRatio
	, (PrivCount/UPrivCount) as UPrivRatio

	from apkInformation ai
	inner join toolResults tr on tr.apkID = ai.rowID
	left outer join (select count (permissionID) as OPrivCount, apkID from overprivilege o group by apkID) overprivCount on overprivCount.apkID = ai.rowID
	left outer join (select count (permissionID) as UPrivCount, apkID from underprivilege u group by apkID) underprivCount on underprivCount.apkID = ai.rowID
	inner join (select count (privID) as PrivCount, rowID from apkParser_privs_join apj group by rowID) permissionCount on permissionCount.rowID = ai.rowID
	where ai.name = '005d5f6e94321de473d62706a94fbecf67c9f5f3'
	
	## Find other info about the specific app

	## Total Permissions
		select apj.rowID, privName 
		from apkParser_privs_join apj
		inner join apkParser_privs ap on ap.privID = apj.privID
		where apj.rowID = 2
		order by Privname

	## What overprivs does it have
		select apkID, p.Name 
		from overprivilege o
		inner join permissions p on p.pid = o.permissionID 
		where apkID = 2

		-- To find the file: find /Users/dxkvse/Desktop/malware/malgenome -type f -name 08a21de6b70f584ceddbe803ae12d79a33d33b50.apk

	#### Find the privs requested by apps
	select privName, count(apj.privID) as PrivCount
	from apkinformation ai
	inner join apkParser_privs_join apj on apj.rowID = ai.rowID
	inner join apkParser_privs ap on ap.privID = apj.privID
	group by apj.privID
	order by PrivName

	### Find the privcount information for each year
	






6 - 
http://www.csc.ncsu.edu/faculty/jiang/RogueLemon/


AnserverBot
10 - http://www.csc.ncsu.edu/faculty/jiang/AnserverBot/


zhash
18 - https://blog.lookout.com/blog/2011/03/20/security-alert-zhash-a-binary-that-can-root-android-phones-found-in-chinese-app-markets-and-android-market/




- overprivs


- too many privs









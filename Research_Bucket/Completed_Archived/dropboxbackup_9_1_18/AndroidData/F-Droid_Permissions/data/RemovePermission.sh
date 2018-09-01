#!/bin/bash          

# Description: Loop through the records in a database and return the person who performed the previous commit
#	Create a comparision between the DCR score of who is adding and removing permissions

clear;


db=Try2AndrosecData.sqlite

## Clear out the target table
sqlite3 $db  "delete from PermissionAdderInfo;"
echo "PermissionAdderInfo Cleared........"



sqlite3 $db "select * from PermissionChange_view pcv where action = 'R' limit 10" | while read dbOutput; do
#   echo $dbOutput
IFS='|' read -a myarray <<< "$dbOutput"


	## test printing out the information

#	echo "0: ${myarray[0]}" ## APPID
#	echo "1: ${myarray[1]}"
#	echo "2: ${myarray[2]}" ## CommitiD
#	echo "3: ${myarray[3]}" ##PermissionID
#	echo "4: ${myarray[4]}"
#	echo "5: ${myarray[5]}"
#	echo "6: ${myarray[6]}"
#	echo "7: ${myarray[7]}" ## Author
#	echo "8: ${myarray[8]}" ## Altered Date


# For each of the returned records, find the person who added the information

	### Should only have 1 record returned
	sqlStatement="select count(AppID) as AppCount from PermissionChange_view where Action = 'A' and appID = ${myarray[0]} and permissionID = ${myarray[3]}  and alteredDate < '${myarray[6]}';"


### Make sure some records are returned from the statement

	sqlResultCount=`sqlite3 $db "$sqlStatement"`

  	if [[ $sqlResultCount -gt 0 ]]; then
      #	echo "Values Found returned For appID = ${myarray[0]} and permissionID = ${myarray[3]}  and alteredDate < '${myarray[6]}'"

		sqlStatement="select max(AlteredDate), AppID, AppName, CommitID, PermissionID, Permission, Action,  alteredDate, Author from PermissionChange_view where Action = 'A' and appID = ${myarray[0]} and permissionID = ${myarray[3]}  and alteredDate < '${myarray[6]}';"
		sqlite3 $db "$sqlStatement" | while read dbOutput; do
		#	sqlite3 $db "select max(AlteredDate), AppID, AppName, CommitID, PermissionID, Permission, Action,  alteredDate, Author from PermissionChange_view where Action = 'A' and appID = ${myarray[0]} and permissionID = ${myarray[3]}  and alteredDate < '${myarray[8]}'" | while read dbOutput; do

			IFS='|' read -a myarray2 <<< "$dbOutput"

		### Now take the returned information and insert it into the new table


		#sqlite3 EvolutionOfAndroidApplications.sqlite  "INSERT INTO ToolResults (ApkId,JlintResult) VALUES ($rowid,$jlintCount);"



			sqlStatement="insert into PermissionAdderInfo (AppID, CommitID, PermissionID, Author, alteredDate) values (${myarray[0]},${myarray[2]},${myarray[3]},'${myarray[7]}','${myarray[6]}');";
			echo $sqlStatement
			sqlite3 $db  "$sqlStatement"




			## Test outputting the values - Can test commenting this all out.
		#	i=0
		#	while [ $i -lt 9 ]
		#	do
		#		echo "$i: ${myarray[i]}"
		#		i=$[$i+1]
		#	done


		#	echo "0: ${myarray2[0]}" ## MaxAlteredDate


			done


		else
			echo "No previous records found: appID = ${myarray[0]} and permissionID = ${myarray[3]}  and alteredDate < '${myarray[6]}'"
    	fi  ## Done checking to make sure that some records were returned





done



### Just test reading the input information. Shut this off in next version
sqlite3 $db  "select * from PermissionAdderInfo"






### Now perform the rest with SQLite and other scripts.....


### Todo
#	Put script into GH
#	


<?php
//call db_connect class
require 'db_connect.php';

//array for JSON response
$myArray = array();

//IF statement for SQL query, runs if query returns values
if($result = $conn->query("SELECT * FROM lobby")){
    
//stores results of the statement in array 
    while($row=$result->fetch_array(MYSQL_ASSOC)){
        $myArray[]=$row;     
    }
    
//echo array encoded in json 
    $myArray["success"]=1;
    echo json_encode($myArray);
	
} else {
//set up failed response and echo 
    $myArray["success"]=0;
    $myArray["message"]= "an error occured";
    echo json_encode($myArray);
}
$result->close();
?>
<?php
//call db_connect class
require 'db_connect.php';

//array for JSON response
$myArray = array();

//check for required fields
if (isset($_GET['name'])) {
//trim id field from HTTP GET and store in variable 
    $name = trim($_GET['name']);

//IF statement for SQL query, runs if query returns values
if($result = $conn->query("SELECT lobbyID FROM lobby WHERE hostName ='$name'")){
    
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
} else {
    //required field missing
    $myArray["success"] = 0;
    $myArray["message"] = "Required field missing";
    //echo json response 
    echo json_encode($myArray);
}


$result->close();
?>
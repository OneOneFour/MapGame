<?php

//array for JSON response
$response = array();

//check for required fields
if (isset($_GET['name'])) {

//trim required fields from HTTP GET and store as variables	
    $name = trim($_GET['name']);	

//call db_connect class 
    require 'db_connect.php';

//DML Query, IF statement triggered if insertion is successful 
    if ($insert = $conn->query("INSERT INTO lobby (HostName) 
        VALUES ('{$name}')")) {
        //successfully inserted
        $response["success"] = 1;
        $response["message"] = "Lobby successfully created.";

        //echo json response
        echo json_encode($response);
    } else {
        //failed to insert row
        $response["success"] = 0;
        $response["message"] = "An error occurred";

        //encode json response
        echo json_encode($response);
    }
} else {
    //required field is missing
    $response["success"] = 0;
    $response["message"] = "Required field is missing";
    //echo json response
    echo json_encode($response);
}

?>
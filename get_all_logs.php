<?php
// call db_connect class
require 'db_connect.php';

//check for required fields
if (isset($_GET['contactID'])) {
//trim id field from HTTP GET and store in variable 
    $id = trim($_GET['contactID']);
    
//IF statement for SQL query, triggered if results found
    if ($stmt = $conn->prepare("SELECT contactlog.date_time,employees.Name 
    FROM contactlog INNER JOIN employees ON contactlog.employeeID = employees.id
    WHERE contactlog.contactID = ? ")) {
//use HTTP GET variable from above in query
        $stmt->bind_param('i', $id);
        $stmt->execute();
        $result = $stmt->get_result();

//store results in array
	while ($row = $result->fetch_array(MYSQL_BOTH)){
	$myArray[]=$row;
	}
        
        //echo json response
        //$myArray["success"] = 1;
        echo json_encode($myArray);
        
    } else {
        //failed
        $myArray["success"] = 0;
        $myArray["message"] = "No Log Found";
        
        //echo json response
        echo json_encode($myArray);
    }
} else {
    //required field missing
    $myArray["success"] = 0;
    $myArray["message"] = "Required field missing";
    //echo json response 
    echo json_encode($myArray);
}
?>
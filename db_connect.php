<?php 
$servername ="127.0.0.1:3306";
$username = "root";
$password = "";
$dbname = "mapgame";

//create connection 
$conn = new mysqli($servername, $username, $password);

//check connection
if ($conn -> connect_error){
	die("Connection failed: " . $conn->connect_error);
}


//select database prints unable if fail

mysqli_select_db($conn, $dbname) or die ("unable to select database");

function close(){
mysql_close();
}

?>
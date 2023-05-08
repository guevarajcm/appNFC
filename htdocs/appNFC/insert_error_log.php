<?php
include 'conexion.php';

// Recuperar los valores enviados desde la aplicación
$error_message = $_POST["error_message"];
$activity_name = $_POST["activity_name"];

// Insertar el registro de error en la tabla logs
$sql = "INSERT INTO logs (error_message, activity_name) VALUES (?, ?)";
$stmt = $conn->prepare($sql);
$stmt->bind_param("ss", $error_message, $activity_name);
$stmt->execute();

// Verificar si la inserción tuvo éxito
if ($stmt->affected_rows > 0) {
    echo "success";
} else {
    echo "Error: " . $sql . "<br>" . $conn->error;
}

$stmt->close();
$conn->close();
?>

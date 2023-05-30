<?php
include 'conexion.php';

$hash = $_POST["shaSum"];
$user_noTrabajador = $_POST["user_noTrabajador"];

$sql = "INSERT INTO otps (hashes, user_noTrabajador) VALUES ('$hash', '$user_noTrabajador')";

if ($conexion->query($sql)) {
    echo "La variable fue insertada en la base de datos";
} else {
    echo "Error al insertar la variable en la base de datos: " . $conn->error;
}


// Cerrar conexión
$sentencia->close();
$conexion->close();
?>
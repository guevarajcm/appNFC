<?php
include 'conexion.php';

$hash = $_POST["shaSum"];

$sql = "INSERT INTO otps (hashes) VALUES ('$hash')";

if ($conexion->query($sql)) {
    echo "La variable fue insertada en la base de datos";
} else {
    echo "Error al insertar la variable en la base de datos: " . $conn->error;
}


// Cerrar conexión
$sentencia->close();
$conexion->close();
?>
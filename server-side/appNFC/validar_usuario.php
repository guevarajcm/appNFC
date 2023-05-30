<?php
include 'conexion.php';

// Recibir datos de la petición
$user_noTrabajador = $_POST['usuario'];
$user_password = $_POST['password'];

// Verificar usuario y contraseña
$sentencia = $conexion->prepare("SELECT * FROM usuarios WHERE user_noTrabajador=? AND user_password=?");
$sentencia->bind_param('ss', $user_noTrabajador, $user_password);
$sentencia->execute();
$resultado = $sentencia->get_result();

if ($fila = $resultado->fetch_assoc()) {
    // Obtener nombre de usuario
    $user_nombres = $fila['user_nombres'];

    // Crear respuesta JSON
    echo json_encode(array("status" => "success", "nombres" => $user_nombres));
} else {
    echo json_encode(array("status" => "error", "message" => "Usuario o contraseña incorrectos."));
}

// Cerrar conexión
$sentencia->close();
$conexion->close();

?>
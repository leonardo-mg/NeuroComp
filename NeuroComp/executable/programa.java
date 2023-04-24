import java.util.Scanner;
public class programa{
public static void main(String[] args){
try{
Scanner scan = new Scanner(System.in);
int opcion;
System.out.print("Elige una opción: \n1.Hola\n2.Adios\n3.Jujis\n4.Salir\n");
opcion = scan.nextInt();
switch(opcion){
case 1:
System.out.print("Hola");
break;
case 2:
System.out.print("Adios");
break;
case 3:
System.out.print("Jujis");
case 4:
System.out.print("saliendo");
break;
default:
System.out.print("Elija una opcion correcta");
}
} catch(Throwable e){
System.out.println("Se ha generado un error en tiempo de ejecucion");
System.out.println("Causa: Posiblemente estas intentando introducir un valor invalido");
}
}
}

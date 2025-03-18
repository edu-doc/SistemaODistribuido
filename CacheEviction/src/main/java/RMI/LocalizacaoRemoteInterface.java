package RMI;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface LocalizacaoRemoteInterface extends Remote {
    int retornaPorta() throws RemoteException;
}

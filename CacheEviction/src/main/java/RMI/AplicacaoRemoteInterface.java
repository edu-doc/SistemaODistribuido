package RMI;

import Banco.No;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface AplicacaoRemoteInterface extends Remote {
    void inserirBackup(No no) throws RemoteException;
}

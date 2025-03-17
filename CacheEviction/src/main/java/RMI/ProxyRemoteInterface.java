package RMI;

import OrdemServico.ServiceOrder;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProxyRemoteInterface extends Remote {
    ServiceOrder buscarEmOutroProxy(int id) throws RemoteException;
}
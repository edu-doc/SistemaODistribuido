package RMI;

import OrdemServico.ServiceOrder;
import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ProxyRemoteInterface extends Remote {
    ServiceOrder buscarEmOutroProxy(int id) throws RemoteException;
    void alterarOutroProxy(int id, String nome, String descricao) throws RemoteException;
    void deletarOutroProxy(int id) throws RemoteException;
}
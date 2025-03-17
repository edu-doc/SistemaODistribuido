package Impl;

import RMI.ProxyRemoteInterface;
import OrdemServico.ServiceOrder;
import Cache.Cache;
import Exception.MyPickException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ImplProxyRmi extends UnicastRemoteObject implements ProxyRemoteInterface {
    private Cache<Integer> cache;

    public ImplProxyRmi(Cache<Integer> cache) throws RemoteException {
        this.cache = cache;
    }

    @Override
    public ServiceOrder buscarEmOutroProxy(int id) throws RemoteException {
        try {
            return cache.get(id); // Busca a OS na cache local
        } catch (MyPickException e) {
            throw new RemoteException("Erro ao buscar OS no proxy remoto: " + e.getMessage());
        }
    }
}
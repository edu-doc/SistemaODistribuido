package Impl;

import Banco.Banco;
import Banco.No;
import OrdemServico.ServiceOrder;
import RMI.AplicacaoRemoteInterface;
import RMI.ProxyRemoteInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

public class ImplAplicacaoRmi extends UnicastRemoteObject implements AplicacaoRemoteInterface {
    private Banco banco;

    public ImplAplicacaoRmi(Banco banco) throws RemoteException {
        this.banco = banco;
    }

    @Override
    public void inserirBackup(No no) throws RemoteException {
        banco.inserir(no);
    }
}

package Impl;

import Banco.Banco;
import Banco.No;
import RMI.AplicacaoRemoteInterface;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;

public class ImplAplicacaoRmi extends UnicastRemoteObject implements AplicacaoRemoteInterface {
    private Banco banco;

    public ImplAplicacaoRmi(Banco banco) throws RemoteException {
        this.banco = banco;
    }

    @Override
    public List<String> inserirBackup(No no) throws RemoteException {
        banco.inserir(no);
        return null;
    }
}

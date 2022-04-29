import java.io.Serializable;
import java.util.Date;

import com.sankhya.util.SessionFile;
import com.sankhya.util.UIDGenerator;

import br.com.sankhya.extensions.actionbutton.AcaoRotinaJava;
import br.com.sankhya.extensions.actionbutton.ContextoAcao;
import br.com.sankhya.extensions.actionbutton.QueryExecutor;
import br.com.sankhya.jape.EntityFacade;
import br.com.sankhya.jape.core.JapeSession;
import br.com.sankhya.modelcore.util.EntityFacadeFactory;
import br.com.sankhya.ws.ServiceContext;

public class ArquivoSemob implements AcaoRotinaJava {
    public void doAction(ContextoAcao contexto) throws Exception {
    	String referencia = contexto.getParam("REFERENCIA").toString();
        Date mDate = new Date(System.currentTimeMillis());
        String nomeArquivo = "SEMOB" + (mDate.getMonth() + 1) + ".xml";
        QueryExecutor query = contexto.getQuery();
        query.setParam("REFERENCIA", contexto.getParam("REFERENCIA"));
        query.nativeSelect("SELECT FUNCIONARIO,"
        		+ "           CPF,"
        		+ "           CARTAO,"
        		+ "           VALOR"
        		+ "      FROM (SELECT LPAD(NVL(FUN.MATRICULA,FUN.CODFUNC),15,'0') AS MATRICULA,"
        		+ "                   SUM(VAL.VLRVALE * VAL.TOTADIAS) AS VALOR,"
        		+ "                   TIP.NROCARTAO AS CARTAO,"
        		+ "                   FUN.NOMEFUNC AS FUNCIONARIO,"
        		+ "                   FC_MASKCPFCNPJ2(FUN.CPF) AS CPF"
        		+ "              FROM TFPFUN FUN"
        		+ "      INNER JOIN AD_TCALCBENEFVALES VAL ON VAL.CODFUNC = FUN.CODFUNC AND VAL.CODEMP = FUN.CODEMP"
        		+ "      INNER JOIN TFPLIN LIN ON LIN.CODLINHA = VAL.NULINHA"
        		+ "      INNER JOIN AD_TTIPVALES TIP ON TIP.CODLINHA = LIN.CODLINHA"
        		+ "             WHERE LIN.CODLINHA = 10"
        		+"				AND VAL.REFERENCIA = {REFERENCIA}"				
        		+ "      GROUP BY FUN.MATRICULA,FUN.CODFUNC,TIP.NROCARTAO,FUN.NOMEFUNC,FUN.CPF)");
        
        StringBuilder conteudoArquivo = new StringBuilder();
        conteudoArquivo.append("<DSImpCEValor>\r\n");
        
        
        JapeSession.SessionHandle hnd = null;
        
        while (query.next()) {
        	conteudoArquivo.append("  <CE>\r\n");
        	conteudoArquivo.append("    <Nome>");
            String funcionario = query.getString("FUNCIONARIO");
            conteudoArquivo.append(funcionario+"</Nome>\r\n");
            conteudoArquivo.append("    <CPF>");
            String cpf = query.getString("CPF");
            conteudoArquivo.append(cpf+"</CPF>\r\n");
            conteudoArquivo.append("    <Cartao>");
            String cartao = query.getString("CARTAO");
            conteudoArquivo.append(cartao+"</Cartao>\r\n");
            conteudoArquivo.append("    <Valor>");
            String valor = query.getString("VALOR");
            conteudoArquivo.append(valor+"</Valor>\r\n");
            conteudoArquivo.append("</CE>");
            conteudoArquivo.append("\r\n");
          
        }
        conteudoArquivo.append("</DSImpCEValor>");
        String chave = "semob_" + UIDGenerator.getNextID();
        hnd = JapeSession.open();
        EntityFacade dwfEntityFacade = EntityFacadeFactory.getDWFFacade();
        byte[] fileContent = conteudoArquivo.toString().getBytes();
        SessionFile sessionFile = SessionFile.createSessionFile((String)nomeArquivo, (String)"text", (byte[])fileContent);
        ServiceContext.getCurrent().putHttpSessionAttribute(chave, (Serializable)sessionFile);
        contexto.setMensagemRetorno("<center><a id=\"alink\" href=\"/mge/visualizadorArquivos.mge?chaveArquivo=" + chave + "\" target=\"_blank\">Baixar Arquivo</center>");
    }
}

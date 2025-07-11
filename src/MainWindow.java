import gals.Lexico;
import gals.Sintatico;
import gals.Semantico;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

import gals.LexicalError;
import gals.SyntacticError;
import gals.SemanticError;

public class MainWindow extends javax.swing.JFrame {

    private JTable tabelaSimbolos;
    private DefaultTableModel tabelaSimbolosModel;

    /**
     * Creates new form MainWindow
     */
    public MainWindow() {
        initComponents();
        sourceInput.setText("function int main() {\r\n" + //
                        "    int a = 1;\r\n" + //
                        "    int b = 2;\r\n" + //
                        "    int c = a + b + 2;\r\n" + //
                        "}");
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        sourceInput = new javax.swing.JTextArea();
        jScrollPane2 = new javax.swing.JScrollPane();
        console = new javax.swing.JTextArea();
        buttonCompile = new javax.swing.JButton();
        buttonAssembly = new javax.swing.JButton();

        tabelaSimbolosModel = new DefaultTableModel(
                new Object[] { "Tipo", "Id", "Escopo", "Vetor", "Função", "Parâmetro", "Inicializada", "Usada" }, 0);
        tabelaSimbolos = new JTable(tabelaSimbolosModel);
        javax.swing.JScrollPane scrollTabela = new javax.swing.JScrollPane(tabelaSimbolos);

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("IDE do Rocha");
        setFont(new java.awt.Font("Andale Mono", 0, 18)); // NOI18N

        sourceInput.setColumns(20);
        sourceInput.setFont(new java.awt.Font("Helvetica Neue", 0, 18)); // NOI18N
        sourceInput.setRows(5);
        jScrollPane1.setViewportView(sourceInput);

        console.setEditable(false);
        console.setColumns(20);
        console.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        console.setLineWrap(true);
        console.setRows(5);
        console.setTabSize(4);
        jScrollPane2.setViewportView(console);

        buttonCompile.setFont(new java.awt.Font("Helvetica Neue", 0, 14)); // NOI18N
        buttonCompile.setText("Compilar");
        buttonCompile.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                buttonCompileActionPerformed(evt);
            }
        });

        buttonAssembly.setFont(new java.awt.Font("Helvetica Neue", 0, 14));
        buttonAssembly.setText("Ver Assembly");
        buttonAssembly.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mostrarAssembly();
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);

        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 457,
                                                Short.MAX_VALUE)
                                        .addComponent(jScrollPane2)
                                        .addComponent(scrollTabela)
                                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING,
                                                layout.createSequentialGroup()
                                                        .addGap(0, 0, Short.MAX_VALUE)
                                                        .addComponent(buttonCompile)
                                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                        .addComponent(buttonAssembly)))
                                .addContainerGap()));
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(layout.createSequentialGroup()
                                .addContainerGap()
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 60,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(scrollTabela, javax.swing.GroupLayout.PREFERRED_SIZE, 100,
                                        javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(3, 3, 3)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(buttonCompile)
                                        .addComponent(buttonAssembly))));

        pack();
    }

    private void buttonCompileActionPerformed(java.awt.event.ActionEvent evt) {
        Lexico lex = new Lexico();
        Sintatico sint = new Sintatico();
        Semantico sem = new Semantico();

        lex.setInput(sourceInput.getText());

        try {
            sint.parse(lex, sem);
            String avisos = sem.avisarNaoUsados();
            String msg = "Compilado com sucesso!";
            if (!avisos.isEmpty()) {
                msg += "\n" + avisos;
            }
            console.setText(msg);

            // Atualiza a tabela de símbolos se disponível
            if (sem.getTabelaSimbolos() != null) {
                atualizarTabelaSimbolos(sem.getTabelaSimbolos().getListSimbolos());
            }
        } catch (LexicalError err) {
            System.err.println("Problema léxico: " + err.getMessage());
            console.setText("Problema léxico: " + err.getMessage());
        } catch (SyntacticError err) {
            System.err.println("Problema sintático: " + err.getMessage());
            console.setText("Problema sintático: " + err.getMessage());
        } catch (SemanticError err) {
            System.err.println("Problema semântico: " + err.getMessage());
            console.setText("Problema semântico: " + err.getMessage());
        }
    }

    // Atualiza a tabela de símbolos na interface
    public void atualizarTabelaSimbolos(java.util.List<compil.Simbolo> simbolos) {
        tabelaSimbolosModel.setRowCount(0); // Limpa a tabela
        for (compil.Simbolo s : simbolos) {
            tabelaSimbolosModel.addRow(new Object[] {
                    s.getTipo(),
                    s.getId(),
                    s.getEscopo(),
                    s.getFlagVetor(),
                    s.getFlagFuncao(),
                    s.getFlagParametro(),
                    s.getFlagInicializada(),
                    s.getFlagUsada()
            });
        }
    }

    // Método para mostrar o código assembly em uma nova janela
    private void mostrarAssembly() {
        // Substitua esta linha pelo seu método real de geração de assembly
        String codigoAssembly = gerarAssembly();

        javax.swing.JFrame frameAssembly = new javax.swing.JFrame("Código Assembly");
        javax.swing.JTextArea areaAssembly = new javax.swing.JTextArea(20, 60);
        areaAssembly.setText(codigoAssembly);
        areaAssembly.setEditable(false);
        areaAssembly.setFont(new java.awt.Font("Monospaced", 0, 14));
        frameAssembly.add(new javax.swing.JScrollPane(areaAssembly));
        frameAssembly.pack();
        frameAssembly.setLocationRelativeTo(this);
        frameAssembly.setVisible(true);
    }

private String gerarAssembly() {
    Lexico lex = new Lexico();
    Sintatico sint = new Sintatico();
    Semantico sem = new Semantico();

    lex.setInput(sourceInput.getText()); // Define o código fonte como entrada

    try {
        // Realiza a análise léxica, sintática e semântica
        sint.parse(lex, sem);

        // Retorna o código assembly gerado
        return sem.gerarCodigoAssembly();
    } catch (LexicalError err) {
        return "Erro léxico: " + err.getMessage();
    } catch (SyntacticError err) {
        return "Erro sintático: " + err.getMessage();
    } catch (SemanticError err) {
        return "Erro semântico: " + err.getMessage();
    }
}

    public static void main(String args[]) {
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (Exception ex) {
            java.util.logging.Logger.getLogger(MainWindow.class.getName()).log(java.util.logging.Level.SEVERE, null,
                    ex);
        }

        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new MainWindow().setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify
    private javax.swing.JButton buttonCompile;
    private javax.swing.JButton buttonAssembly;
    private javax.swing.JTextArea console;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea sourceInput;
    // End of variables declaration
}
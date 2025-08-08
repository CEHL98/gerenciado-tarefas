/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Other/SQLTemplate.sql to edit this template
 */
/**
 * Author:  Particular
 * Created: 6 de ago. de 2025
 */

CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS tarefa (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    descricao VARCHAR(255) NOT NULL,
    prioridade VARCHAR(10),
    status VARCHAR(10) NOT NULL DEFAULT 'TO_DO',
    usuario_id BIGINT NOT NULL,
    FOREIGN KEY (usuario_id) REFERENCES usuario(id),
    CONSTRAINT chk_prioridade CHECK (prioridade IN ('ALTA', 'MEDIA', 'BAIXA')),
    CONSTRAINT chk_status CHECK (status IN ('TO_DO', 'DOING', 'DONE'))
);
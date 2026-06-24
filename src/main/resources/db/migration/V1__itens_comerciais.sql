CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE itens_comerciais (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    tipo_item VARCHAR(31) NOT NULL,
    nome VARCHAR(255) NOT NULL,
    descricao TEXT,
    preco_base NUMERIC(19, 2) NOT NULL,
    ativo BOOLEAN NOT NULL DEFAULT TRUE,
    quantidade_estoque INTEGER NOT NULL DEFAULT 0,
    estoque_minimo INTEGER NOT NULL DEFAULT 0,
    estoque_maximo INTEGER NOT NULL DEFAULT 0,
    fabricante VARCHAR(255),
    codigo_fabricante VARCHAR(255),
    modelo_peca VARCHAR(255),
    unidade_medida VARCHAR(50),
    created_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW(),
    updated_at TIMESTAMP WITHOUT TIME ZONE DEFAULT NOW()
);

CREATE INDEX idx_itens_comerciais_tipo ON itens_comerciais(tipo_item);
CREATE INDEX idx_itens_comerciais_nome ON itens_comerciais(nome);
CREATE INDEX idx_itens_comerciais_estoque ON itens_comerciais(quantidade_estoque);

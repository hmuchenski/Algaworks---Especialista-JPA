package com.algaworks.ecommerce.model;

import java.math.BigDecimal;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

//@IdClass(value = ItemPedidoId.class)
@Entity
@Table(name = "item_pedido")
public class ItemPedido {

	// @Id
	// @Column(name = "pedido_id")
	// private Integer pedidoId;

	// @Id
	// @Column(name = "produto_id")
	// private Integer produtoId;

	@EmbeddedId
	private ItemPedidoId id;

	@MapsId("pedidoId") // Mapeia o atributo pedidoId do EmbeddedId para a chave primária da entidade Pedido.
	// O valor de id.pedidoId será obtido a partir da chave primária da entidade Pedido.
	@ManyToOne(optional = false)
	@JoinColumn(name = "pedido_id")
	private Pedido pedido;

	@MapsId("produtoId") // Mapeia o atributo produtoId do EmbeddedId para a chave primária da entidade Produto.
	// O valor de id.produtoId será obtido a partir da chave primária da entidade Produto.
	@ManyToOne(optional = false)
	@JoinColumn(name = "produto_id")
	private Produto produto;

	@Column(name = "preco_produto")
	private BigDecimal precoProduto;

	private Integer quantidade;

	public ItemPedido() {
		super();
	}

//	public Integer getPedidoId() {
//		return pedidoId;
//	}
//
//	public void setPedidoId(Integer pedidoId) {
//		this.pedidoId = pedidoId;
//	}
//
//	public Integer getProdutoId() {
//		return produtoId;
//	}
//
//	public void setProdutoId(Integer produtoId) {
//		this.produtoId = produtoId;
//	}

	public Pedido getPedido() {
		return pedido;
	}

	public void setPedido(Pedido pedido) {
		this.pedido = pedido;
	}

	public Produto getProduto() {
		return produto;
	}

	public void setProduto(Produto produto) {
		this.produto = produto;
	}

	public BigDecimal getPrecoProduto() {
		return precoProduto;
	}

	public void setPrecoProduto(BigDecimal precoProduto) {
		this.precoProduto = precoProduto;
	}

	public Integer getQuantidade() {
		return quantidade;
	}

	public void setQuantidade(Integer quantidade) {
		this.quantidade = quantidade;
	}

	public ItemPedidoId getId() {
		return id;
	}

	public void setId(ItemPedidoId id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return Objects.hash(id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ItemPedido other = (ItemPedido) obj;
		return Objects.equals(id, other.id);
	}

}

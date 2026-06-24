package com.algaworks.ecommerce.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "pedido")
public class Pedido {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Integer id;

	@Column(name = "pedido_id")
	private Integer pedidoId;

	@Column(name = "data_pedido")
	private LocalDateTime dataPedido;

	@Column(name = "data_conclusao")
	private LocalDateTime dataConclusao;

	@Column(name = "nota_fiscal_id")
	private Integer notaFiscalId;

	private BigDecimal total;

	@Enumerated(EnumType.STRING)
	private StatusPedido status;

	@Embedded
	private EnderecoEntregaPedido enderecoEntrega;

	@ManyToOne
	@JoinColumn(name = "cliente_id")
	private Cliente cliente;

	@OneToMany(mappedBy = "pedido")
	private List<ItemPedido> itensPedidos;

	public Pedido() {
		super();
	}

	public Pedido(Integer id, LocalDateTime dataPedido, LocalDateTime dataConclusao, Integer notaFiscalId,
			BigDecimal total, StatusPedido status, EnderecoEntregaPedido enderecoEntrega, Cliente cliente,
			List<ItemPedido> itensPedidos) {
		super();
		this.id = id;
		this.dataPedido = dataPedido;
		this.dataConclusao = dataConclusao;
		this.notaFiscalId = notaFiscalId;
		this.total = total;
		this.status = status;
		this.enderecoEntrega = enderecoEntrega;
		this.cliente = cliente;
		this.itensPedidos = itensPedidos;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LocalDateTime getDataPedido() {
		return dataPedido;
	}

	public void setDataPedido(LocalDateTime dataPedido) {
		this.dataPedido = dataPedido;
	}

	public LocalDateTime getDataConclusao() {
		return dataConclusao;
	}

	public void setDataConclusao(LocalDateTime dataConclusao) {
		this.dataConclusao = dataConclusao;
	}

	public Integer getNotaFiscalId() {
		return notaFiscalId;
	}

	public void setNotaFiscalId(Integer notaFiscalId) {
		this.notaFiscalId = notaFiscalId;
	}

	public BigDecimal getTotal() {
		return total;
	}

	public void setTotal(BigDecimal total) {
		this.total = total;
	}

	public StatusPedido getStatus() {
		return status;
	}

	public void setStatus(StatusPedido status) {
		this.status = status;
	}

	public EnderecoEntregaPedido getEnderecoEntrega() {
		return enderecoEntrega;
	}

	public void setEnderecoEntrega(EnderecoEntregaPedido enderecoEntrega) {
		this.enderecoEntrega = enderecoEntrega;
	}

	public Cliente getCliente() {
		return cliente;
	}

	public void setCliente(Cliente cliente) {
		this.cliente = cliente;
	}

	public List<ItemPedido> getItensPedidos() {
		return itensPedidos;
	}

	public void setItensPedidos(List<ItemPedido> itensPedidos) {
		this.itensPedidos = itensPedidos;
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
		Pedido other = (Pedido) obj;
		return Objects.equals(id, other.id);
	}

}

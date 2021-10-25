package com.aglayatech.licorstore.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.util.ResourceUtils;

import com.aglayatech.licorstore.model.Factura;
import com.aglayatech.licorstore.repository.IFacturaRepository;
import com.aglayatech.licorstore.service.IFacturaService;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

@Service
public class FacturaServiceImpl implements IFacturaService {

	@Autowired
	private IFacturaRepository repoFactura;
	
	@Autowired
	protected DataSource localDataSource;
	
	@Override
	public List<Factura> findAll() {
		return repoFactura.findAll(Sort.by(Direction.DESC, "fecha"));
	}

	@Override
	public Page<Factura> findAll(Pageable pageable) {
		return repoFactura.findAll(pageable);
	}

	@Override
	public Factura findFactura(Long idfactura) {
		return repoFactura.findById(idfactura).orElse(null);
	}

	@Override
	public Factura save(Factura factura) {
		return repoFactura.save(factura);
	}

	/****************** PDF REPORT SERVICES *******************/
	
	// REPORTE DE VENTAS DIARIAS
	@Override
	public byte[] resportDailySales(Integer usuario, Date fecha) 
			throws JRException, FileNotFoundException, SQLException {
		
		Connection con = localDataSource.getConnection(); // Obtiene la conexión actual a la base de datos
		Map<String, Object> params = new HashMap<>();
		File file = ResourceUtils.getFile("classpath:\\reports\\rpt_ventas_diarias.jrxml");
		params.put("usuario", usuario);
		params.put("fecha", fecha);
		
		JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, con);
		
		ByteArrayOutputStream byteArrayOutputStream = getByteArrayOutputStream(jasperPrint);
		
		con.close();
		return byteArrayOutputStream.toByteArray();
	}
	
	

	// GENERADOR DE REPORTE DE FACTURA
	@Override
	public byte[] showBill(Long idfactura) 
			throws JRException, FileNotFoundException, SQLException {
		
		Connection con = localDataSource.getConnection();
		Map<String, Object> params = new HashMap<>();
		params.put("idfactura", idfactura);
		File file = ResourceUtils.getFile("classpath:\\reports\\rpt_factura.jrxml");
		
		JasperReport jasperReport = JasperCompileManager.compileReport(file.getAbsolutePath());
		JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, con);
		
		ByteArrayOutputStream byteArrayOutputStream = getByteArrayOutputStream(jasperPrint);
		
		con.close();
		return byteArrayOutputStream.toByteArray();
	}
	
	protected ByteArrayOutputStream getByteArrayOutputStream(JasperPrint jasperPrint) throws JRException {
	    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
	    JasperExportManager.exportReportToPdfStream(jasperPrint, byteArrayOutputStream);
	    return byteArrayOutputStream;
	}

}

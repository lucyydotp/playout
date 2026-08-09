package me.lucyydotp.playout.controller.server.amcp

import java.io.StringWriter
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult
import me.lucyydotp.playout.config.PlayoutConfig
import me.lucyydotp.playout.controller.env.EnvironmentVariables
import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.Node

context(document: Document, node: Node)
private inline operator fun String.invoke(fn: Element.() -> Unit) =
    node.appendChild(document.createElement(this).apply(fn))

/** Gets the config as a CasparCG-compatible XML string. */
public fun PlayoutConfig.asCasparCompatibleXml(): String {
    val document = DocumentBuilderFactory.newDefaultInstance().newDocumentBuilder().newDocument()

    with(document) {
        "configuration" {
            "paths" {
                "media-path" { textContent = "media" }
                "data-path" { textContent = "data" }
                "template-path" { textContent = "templates" }
                "log-path" { textContent = "logs" }
            }

            "channels" {
                outputs.forEach { (_, config) ->
                    "channel" { "video-mode" { textContent = "${config.resolution.height}p6000" } }
                }
            }

            if (EnvironmentVariables.amcpScannerPort != null) {
                "amcp" {
                    "media-server" {
                        "host" { textContent = EnvironmentVariables.amcpScannerAdvertisedHost }
                        "port" { textContent = EnvironmentVariables.amcpScannerPort?.toString() }
                    }
                }
            }
        }
    }

    return StringWriter()
        .also {
            TransformerFactory.newInstance()
                .newTransformer()
                .transform(DOMSource(document), StreamResult(it))
        }
        .toString()
}

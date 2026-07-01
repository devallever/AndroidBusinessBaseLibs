package app.flash.tunnel.vpn.page.viewmodel

import app.flash.tunnel.vpn.lib.common.base.AbsViewModel

class FAQViewModel : AbsViewModel() {
    val questions by lazy {
        listOf(
            "Q1. What is FlashTunnel network accelerator?",
            "Q2. How does a network accelerator work?",
            "Q3. What are the benefits of using a network accelerator?",
            "Q4. Can a network accelerator improve online gaming performance?",
            "Q5. Is it safe to use a network accelerator?"
        )
    }
    val answers by lazy {
        listOf(
            "FlashTunnel is a high-performance network accelerator designed to improve internet speed and stability. It uses advanced technology to optimize network connections, reduce latency, and enhance the overall online experience. By utilizing a global network of servers, FlashTunnel can efficiently bypass internet traffic congestion and deliver faster data transmission for users. Additionally, it offers secure encryption to protect data privacy and ensure a safe browsing environment. With its user-friendly interface, FlashTunnel provides easy setup and seamless integration with various devices and operating systems. Whether for personal or business use, FlashTunnel is an effective solution for individuals and organizations seeking to maximize their internet performance.",
            "A network accelerator works by optimizing internet connections and reducing latency to improve data transmission speed. It achieves this through various techniques such as data compression, caching, and protocol optimization. By minimizing the impact of network congestion and packet loss, a network accelerator can effectively enhance the overall internet experience for users.",
            "Using a network accelerator can provide several benefits, including faster internet speed, improved stability, reduced latency, and enhanced security. It can also optimize data transmission for streaming, gaming, and other online activities, leading to a better user experience.",
            "Yes, a network accelerator can significantly improve online gaming performance by reducing latency, optimizing data transmission, and providing a more stable internet connection. This can result in smoother gameplay, reduced lag, and overall better gaming experience for players.",
            "When using a reputable and trusted network accelerator like FlashTunnel, it is safe to use as it employs secure encryption to protect data privacy and ensure a safe browsing environment. However, it is important to choose a reliable network accelerator to avoid potential security risks."
        )
    }
}
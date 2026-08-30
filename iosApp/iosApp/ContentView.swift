import GoogleMobileAds
import SwiftUI
import UIKit
import core

struct ComposeView: UIViewControllerRepresentable {
    var deepLinkUrl: String?

    func makeUIViewController(context: Context) -> UIViewController {
        if let url = deepLinkUrl {
            DeepLinkState.shared.url.value = url
        }
        return MainViewControllerKt.MainViewController(
            bannerProvider: {
                let banner = BannerView()
                banner.load(Request())
                return banner
            }
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {
        if let url = deepLinkUrl {
            DeepLinkState.shared.url.value = url
        }
    }
}

struct ContentView: View {
    var deepLinkUrl: String?

    var body: some View {
        ComposeView(deepLinkUrl: deepLinkUrl)
            .ignoresSafeArea()
    }
}

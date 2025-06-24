import GoogleMobileAds
import SwiftUI
import UIKit
import core

struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController(
            bannerProvider: {
                let banner = BannerView()
                banner.adUnitID = "ca-app-pub-2361666372543198/2292302980"
                banner.load(Request())
                return banner
            }
        )
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        ComposeView()
            .ignoresSafeArea()
//            .ignoresSafeArea(.keyboard)  // Compose has own keyboard handler
    }
}

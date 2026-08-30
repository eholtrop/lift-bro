import SwiftUI

@main
struct iOSApp: App {
	@State private var deepLinkUrl: String?

	init() {
	}

	var body: some Scene {
		WindowGroup {
			ContentView(deepLinkUrl: deepLinkUrl)
				.onOpenURL { url in
					deepLinkUrl = url.absoluteString
				}
		}
	}
}

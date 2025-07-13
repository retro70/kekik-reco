// Kekik Reco - Türkçe İçerik Sağlayıcı
// Cloudstream için birleşik Türkçe streaming eklentisi
// @retro70 tarafından geliştirilmiştir

using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using CloudStream.Core;
using CloudStream.Core.Models;
using CloudStream.Core.Providers;
using CloudStream.Core.Utils;

namespace KekikReco
{
    public class KekikRecoProvider : BaseProvider
    {
        public override string Name => "Kekik Reco";
        public override string Language => "tr";
        public override string BaseUrl => "https://kekik-reco.com";
        
        private readonly List<BaseProvider> _providers;
        
        public KekikRecoProvider()
        {
            _providers = new List<BaseProvider>
            {
                new FilmIzleseneProvider(),
                new SetFilmIzleProvider(),
                new HDFilmIzleProvider(),
                new DiziBoxProvider(),
                new AnimeIzleseneProvider(),
                new CanliTVProvider(),
                new BelgeselXProvider(),
                new KultFilmlerProvider(),
                new NetflixMirrorProvider(),
                new YouTubeProvider()
            };
        }

        public override async Task<List<SearchResult>> Search(string query)
        {
            var results = new List<SearchResult>();
            
            foreach (var provider in _providers)
            {
                try
                {
                    var providerResults = await provider.Search(query);
                    results.AddRange(providerResults);
                }
                catch (Exception ex)
                {
                    Logger.LogError($"Error searching {provider.Name}: {ex.Message}");
                }
            }
            
            return results.Distinct().ToList();
        }

        public override async Task<MediaInfo> GetMediaInfo(string url)
        {
            foreach (var provider in _providers)
            {
                try
                {
                    if (provider.CanHandle(url))
                    {
                        return await provider.GetMediaInfo(url);
                    }
                }
                catch (Exception ex)
                {
                    Logger.LogError($"Error getting media info from {provider.Name}: {ex.Message}");
                }
            }
            
            throw new Exception("No provider can handle this URL");
        }

        public override async Task<List<StreamInfo>> GetStreams(string url)
        {
            foreach (var provider in _providers)
            {
                try
                {
                    if (provider.CanHandle(url))
                    {
                        return await provider.GetStreams(url);
                    }
                }
                catch (Exception ex)
                {
                    Logger.LogError($"Error getting streams from {provider.Name}: {ex.Message}");
                }
            }
            
            throw new Exception("No provider can handle this URL");
        }

        public override bool CanHandle(string url)
        {
            return _providers.Any(p => p.CanHandle(url));
        }
    }

    // Film İzlesene Provider
    public class FilmIzleseneProvider : BaseProvider
    {
        public override string Name => "Film İzlesene";
        public override string Language => "tr";
        public override string BaseUrl => "https://www.filmizlesene.plus";

        public override async Task<List<SearchResult>> Search(string query)
        {
            var results = new List<SearchResult>();
            var searchUrl = $"{BaseUrl}/?s={Uri.EscapeDataString(query)}";
            
            try
            {
                var html = await Http.GetStringAsync(searchUrl);
                var doc = Html.Parse(html);
                
                var items = doc.SelectNodes("//div[@class='movie-box']");
                foreach (var item in items)
                {
                    var title = item.SelectSingleNode(".//div[@class='film-ismi']//a")?.InnerText?.Trim();
                    var url = item.SelectSingleNode(".//div[@class='film-ismi']//a")?.GetAttributeValue("href", "");
                    var poster = item.SelectSingleNode(".//div[@class='poster']//img")?.GetAttributeValue("data-src", "");
                    
                    if (!string.IsNullOrEmpty(title) && !string.IsNullOrEmpty(url))
                    {
                        results.Add(new SearchResult
                        {
                            Title = title,
                            Url = url,
                            PosterUrl = poster,
                            Provider = Name
                        });
                    }
                }
            }
            catch (Exception ex)
            {
                Logger.LogError($"Film İzlesene search error: {ex.Message}");
            }
            
            return results;
        }

        public override async Task<MediaInfo> GetMediaInfo(string url)
        {
            try
            {
                var html = await Http.GetStringAsync(url);
                var doc = Html.Parse(html);
                
                var title = doc.SelectSingleNode("//div[@class='title-border']//h1")?.InnerText?.Trim();
                var description = doc.SelectSingleNode("//div[@id='film-aciklama']")?.InnerText?.Trim();
                var poster = doc.SelectSingleNode("//div[@class='film-afis']//img")?.GetAttributeValue("src", "");
                var year = doc.SelectSingleNode("//div[@class='release']//a")?.InnerText?.Trim();
                
                return new MediaInfo
                {
                    Title = title,
                    Description = description,
                    PosterUrl = poster,
                    Year = year,
                    Provider = Name
                };
            }
            catch (Exception ex)
            {
                Logger.LogError($"Film İzlesene media info error: {ex.Message}");
                throw;
            }
        }

        public override async Task<List<StreamInfo>> GetStreams(string url)
        {
            var streams = new List<StreamInfo>();
            
            try
            {
                var html = await Http.GetStringAsync(url);
                var doc = Html.Parse(html);
                
                var sources = doc.SelectNodes("//div[@class='sources']//script");
                foreach (var source in sources)
                {
                    var scriptContent = source.InnerText;
                    if (scriptContent.Contains("#source"))
                    {
                        var iframeUrl = ExtractIframeUrl(scriptContent);
                        if (!string.IsNullOrEmpty(iframeUrl))
                        {
                            streams.Add(new StreamInfo
                            {
                                Url = iframeUrl,
                                Quality = "HD",
                                Provider = Name
                            });
                        }
                    }
                }
            }
            catch (Exception ex)
            {
                Logger.LogError($"Film İzlesene streams error: {ex.Message}");
            }
            
            return streams;
        }

        private string ExtractIframeUrl(string scriptContent)
        {
            // Basit iframe URL çıkarma
            var startIndex = scriptContent.IndexOf("<iframe src=\"");
            if (startIndex >= 0)
            {
                startIndex += 13;
                var endIndex = scriptContent.IndexOf("\"", startIndex);
                if (endIndex > startIndex)
                {
                    return scriptContent.Substring(startIndex, endIndex - startIndex);
                }
            }
            return null;
        }

        public override bool CanHandle(string url)
        {
            return url.Contains("filmizlesene");
        }
    }

    // Set Film İzle Provider
    public class SetFilmIzleProvider : BaseProvider
    {
        public override string Name => "Set Film İzle";
        public override string Language => "tr";
        public override string BaseUrl => "https://www.setfilmizle.lol";

        public override async Task<List<SearchResult>> Search(string query)
        {
            var results = new List<SearchResult>();
            var searchUrl = $"{BaseUrl}/?s={Uri.EscapeDataString(query)}";
            
            try
            {
                var html = await Http.GetStringAsync(searchUrl);
                var doc = Html.Parse(html);
                
                var items = doc.SelectNodes("//div[@class='result-item']//article");
                foreach (var item in items)
                {
                    var title = item.SelectSingleNode(".//div[@class='title']//a")?.InnerText?.Trim();
                    var url = item.SelectSingleNode(".//div[@class='title']//a")?.GetAttributeValue("href", "");
                    var poster = item.SelectSingleNode(".//img")?.GetAttributeValue("src", "");
                    
                    if (!string.IsNullOrEmpty(title) && !string.IsNullOrEmpty(url))
                    {
                        results.Add(new SearchResult
                        {
                            Title = title,
                            Url = url,
                            PosterUrl = poster,
                            Provider = Name
                        });
                    }
                }
            }
            catch (Exception ex)
            {
                Logger.LogError($"Set Film İzle search error: {ex.Message}");
            }
            
            return results;
        }

        public override async Task<MediaInfo> GetMediaInfo(string url)
        {
            try
            {
                var html = await Http.GetStringAsync(url);
                var doc = Html.Parse(html);
                
                var title = doc.SelectSingleNode("//h1")?.InnerText?.Trim();
                var description = doc.SelectSingleNode("//div[@class='wp-content']//p")?.InnerText?.Trim();
                var poster = doc.SelectSingleNode("//div[@class='poster']//img")?.GetAttributeValue("src", "");
                var year = doc.SelectSingleNode("//div[@class='extra']//span[@class='C']//a")?.InnerText?.Trim();
                
                return new MediaInfo
                {
                    Title = title,
                    Description = description,
                    PosterUrl = poster,
                    Year = year,
                    Provider = Name
                };
            }
            catch (Exception ex)
            {
                Logger.LogError($"Set Film İzle media info error: {ex.Message}");
                throw;
            }
        }

        public override async Task<List<StreamInfo>> GetStreams(string url)
        {
            // Set Film İzle için stream çıkarma implementasyonu
            return new List<StreamInfo>();
        }

        public override bool CanHandle(string url)
        {
            return url.Contains("setfilmizle");
        }
    }

    // Diğer provider'lar için placeholder sınıflar
    public class HDFilmIzleProvider : BaseProvider
    {
        public override string Name => "HD Film İzle";
        public override string Language => "tr";
        public override string BaseUrl => "https://hdfilmizle.com";

        public override async Task<List<SearchResult>> Search(string query) => new List<SearchResult>();
        public override async Task<MediaInfo> GetMediaInfo(string url) => new MediaInfo();
        public override async Task<List<StreamInfo>> GetStreams(string url) => new List<StreamInfo>();
        public override bool CanHandle(string url) => url.Contains("hdfilmizle");
    }

    public class DiziBoxProvider : BaseProvider
    {
        public override string Name => "Dizi Box";
        public override string Language => "tr";
        public override string BaseUrl => "https://dizibox.com";

        public override async Task<List<SearchResult>> Search(string query) => new List<SearchResult>();
        public override async Task<MediaInfo> GetMediaInfo(string url) => new MediaInfo();
        public override async Task<List<StreamInfo>> GetStreams(string url) => new List<StreamInfo>();
        public override bool CanHandle(string url) => url.Contains("dizibox");
    }

    public class AnimeIzleseneProvider : BaseProvider
    {
        public override string Name => "Anime İzlesene";
        public override string Language => "tr";
        public override string BaseUrl => "https://animeizlesene.com";

        public override async Task<List<SearchResult>> Search(string query) => new List<SearchResult>();
        public override async Task<MediaInfo> GetMediaInfo(string url) => new MediaInfo();
        public override async Task<List<StreamInfo>> GetStreams(string url) => new List<StreamInfo>();
        public override bool CanHandle(string url) => url.Contains("animeizlesene");
    }

    public class CanliTVProvider : BaseProvider
    {
        public override string Name => "Canlı TV";
        public override string Language => "tr";
        public override string BaseUrl => "https://canlitv.com";

        public override async Task<List<SearchResult>> Search(string query) => new List<SearchResult>();
        public override async Task<MediaInfo> GetMediaInfo(string url) => new MediaInfo();
        public override async Task<List<StreamInfo>> GetStreams(string url) => new List<StreamInfo>();
        public override bool CanHandle(string url) => url.Contains("canlitv");
    }

    public class BelgeselXProvider : BaseProvider
    {
        public override string Name => "Belgesel X";
        public override string Language => "tr";
        public override string BaseUrl => "https://belgeselx.com";

        public override async Task<List<SearchResult>> Search(string query) => new List<SearchResult>();
        public override async Task<MediaInfo> GetMediaInfo(string url) => new MediaInfo();
        public override async Task<List<StreamInfo>> GetStreams(string url) => new List<StreamInfo>();
        public override bool CanHandle(string url) => url.Contains("belgeselx");
    }

    public class KultFilmlerProvider : BaseProvider
    {
        public override string Name => "Kült Filmler";
        public override string Language => "tr";
        public override string BaseUrl => "https://kultfilmler.com";

        public override async Task<List<SearchResult>> Search(string query) => new List<SearchResult>();
        public override async Task<MediaInfo> GetMediaInfo(string url) => new MediaInfo();
        public override async Task<List<StreamInfo>> GetStreams(string url) => new List<StreamInfo>();
        public override bool CanHandle(string url) => url.Contains("kultfilmler");
    }

    public class NetflixMirrorProvider : BaseProvider
    {
        public override string Name => "Netflix Mirror";
        public override string Language => "tr";
        public override string BaseUrl => "https://netflixmirror.com";

        public override async Task<List<SearchResult>> Search(string query) => new List<SearchResult>();
        public override async Task<MediaInfo> GetMediaInfo(string url) => new MediaInfo();
        public override async Task<List<StreamInfo>> GetStreams(string url) => new List<StreamInfo>();
        public override bool CanHandle(string url) => url.Contains("netflixmirror");
    }

    public class YouTubeProvider : BaseProvider
    {
        public override string Name => "YouTube";
        public override string Language => "tr";
        public override string BaseUrl => "https://youtube.com";

        public override async Task<List<SearchResult>> Search(string query) => new List<SearchResult>();
        public override async Task<MediaInfo> GetMediaInfo(string url) => new MediaInfo();
        public override async Task<List<StreamInfo>> GetStreams(string url) => new List<StreamInfo>();
        public override bool CanHandle(string url) => url.Contains("youtube");
    }

    // Ana provider'ı kaydet
    public static class KekikRecoPlugin
    {
        public static void Register()
        {
            ProviderManager.RegisterProvider(new KekikRecoProvider());
        }
    }
}
